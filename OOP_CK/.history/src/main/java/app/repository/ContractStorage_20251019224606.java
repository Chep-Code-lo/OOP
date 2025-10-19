package app.repository;
import app.repository.DataStore;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ContractStorage {
    private static final Path SAVE_PATH    = Paths.get("data", "contracts.csv");
    private static final Path COUNTER_PATH = Paths.get("data", "id_counters.properties");
    private static final int  ID_WIDTH     = 4; // -> Co0001, Ch0001 (đổi 5 nếu muốn Co00001)
    private static final String HEADER = "id/status/name/money/phone/vayDate/traDate/interest/typeInterest/note/createdAt";
    private static final Set<String> PERSISTED_IDS = new LinkedHashSet<>();
    private static boolean initialized = false;
    private static boolean dirty = false;

    // ====== API TẠO MỚI (giữ nguyên hành vi cũ) ======
    public static synchronized void saveBorrow(String status,
                                               String name,
                                               String money,
                                               String phone,
                                               String vayDate,
                                               String traDate,
                                               double interest,
                                               String type,
                                               String note) throws IOException {
        ensureLoaded();
        Contract entry = new Contract(
                nextIdForStatus(status),
                status,
                name,
                appendCurrency(money),
                phone,
                vayDate,
                traDate,
                Double.toString(interest),
                type,
                note,
                LocalDateTime.now().toString()
        );
        DataStore.upsertLoan(toStoreRow(entry));
        dirty = true;
        persistCurrent();
    }

    // ====== Sinh ID tuần tự theo stats (giữ nguyên) ======
    private static synchronized String nextIdForStatus(String status) throws IOException {
        String prefix = statusPrefix(status); // CoNo -> "Co", ChNo -> "Ch"

        Properties p = new Properties();
        if (Files.exists(COUNTER_PATH)) {
            try (var in = Files.newInputStream(COUNTER_PATH)) { p.load(in); }
        }
        int last = Integer.parseInt(p.getProperty(prefix, "0"));
        int next = last + 1;

        p.setProperty(prefix, Integer.toString(next));
        Files.createDirectories(COUNTER_PATH.getParent());
        try (var out = Files.newOutputStream(COUNTER_PATH,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            p.store(out, "ID counters per prefix (Co/Ch)");
        }

        return prefix + String.format("%0" + ID_WIDTH + "d", next);
    }

    private static String statusPrefix(String status) {
        if (status == null) return "Co";
        switch (status) {
            case "CoNo": return "Co";
            case "ChNo": return "Ch";
            default:     return status.substring(0, Math.min(2, status.length()));
        }
    }

    private static String esc(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    // =================== BỔ SUNG: DTO & UPDATE ===================

    /** Bản ghi hợp đồng (DTO) tương ứng 1 dòng CSV */
    public static class Contract {
        public String id;
        public String status;
        public String name;
        public String money;    // định dạng hiển thị kèm hậu tố VND
        public String phone;
        public String vayDate;
        public String traDate;
        public String interest;
        public String type;
        public String note;
        public String createdAt;

        public Contract() {}

        public Contract(String id, String status, String name, String money, String phone,
                        String vayDate,String traDate, String interest, String type, String note, String createdAt) {
            this.id = id;
            this.status = status;
            this.name = name;
            this.money = money;
            this.phone = phone;
            this.vayDate = vayDate;
            this.traDate = traDate;
            this.interest = interest;
            this.type = type;
            this.note = note;
            this.createdAt = createdAt;
        }

        public String toCsvLine() {
            return String.join("/" ,
                    esc(id), esc(status), esc(name), esc(money), esc(phone),
                    esc(vayDate),esc(traDate), interest, esc(type), esc(note), esc(createdAt)
            );
        }
    }

    // Trong ContractStorage.java

    public static synchronized List<Contract> loadAll() throws IOException {
        ensureLoaded();
        return currentContracts();
    }

    public static synchronized List<Contract> Makecell() throws IOException {
        ensureLoaded();
        return currentContracts();
    }

    // Helper: đọc 1 cột theo tên (case-insensitive), tự gỡ dấu "..."
    private static String cell(String[] parts, Map<String,Integer> pos, String key) {
        Integer i = pos.get(key);
        if (i == null || i < 0 || i >= parts.length) return "";
        String s = parts[i];
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }
    private static String firstNonEmpty(String... ss) {
        for (String s : ss) if (s != null && !s.isBlank()) return s;
        return "";
    }



    /** Đồng bộ toàn bộ danh sách vào DataStore (không ghi CSV). */
    public static synchronized void saveAll(List<Contract> list) throws IOException {
        ensureLoaded();
        DataStore.replaceLoans(list.stream()
                .map(ContractStorage::toStoreRow)
                .collect(Collectors.toList()));
        dirty = true;
        persistCurrent();
    }

    /** Ghi dữ liệu đang có trong DataStore ra CSV khi người dùng yêu cầu. */
    public static synchronized int flushPending() throws IOException {
        ensureLoaded();
        List<Contract> list = currentContracts();
        Set<String> currentIds = list.stream()
                .map(c -> safe(c.id))
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> newIds = new LinkedHashSet<>(currentIds);
        newIds.removeAll(PERSISTED_IDS);

        if (dirty || !newIds.isEmpty()) {
            persistCurrent(list, currentIds);
        }

        return newIds.size();
    }

    /** Cập nhật theo số thứ tự (1-based index). Giữ nguyên id/createdAt. */
    public static synchronized boolean updateByIndex(int oneBasedIndex, Contract updated) throws IOException {
        ensureLoaded();
        List<Contract> list = currentContracts();
        if (oneBasedIndex < 1 || oneBasedIndex > list.size()) return false;
        int idx = oneBasedIndex - 1;
        Contract old = list.get(idx);

        // Giữ id và createdAt cũ
        updated.id = old.id;
        updated.createdAt = old.createdAt;

        list.set(idx, updated);
        DataStore.replaceLoans(list.stream()
                .map(ContractStorage::toStoreRow)
                .collect(Collectors.toList()));
        dirty = true;
        persistCurrent();
        return true;
    }

    private static String appendCurrency(String money) {
        if (money == null) return "";
        String trimmed = money.trim();
        if (trimmed.isEmpty()) return "";
        if (trimmed.toUpperCase(Locale.ROOT).endsWith("VND")) {
            return trimmed;
        }
        return trimmed + " VND";
    }

    private static void ensureLoaded() throws IOException {
        if (initialized) return;
        List<Contract> fromCsv = readAllFromCsv();
        DataStore.replaceLoans(fromCsv.stream()
                .map(ContractStorage::toStoreRow)
                .collect(Collectors.toList()));
        PERSISTED_IDS.clear();
        for (Contract c : fromCsv) {
            if (c.id != null && !c.id.isBlank()) {
                PERSISTED_IDS.add(c.id);
            }
        }
        initialized = true;
        dirty = false;
    }

    private static List<Contract> currentContracts() {
        List<Contract> list = new ArrayList<>();
        for (Map<String, Object> row : DataStore.loans()) {
            list.add(fromStoreRow(row));
        }
        return list;
    }

    private static void persistCurrent() throws IOException {
        List<Contract> list = currentContracts();
        Set<String> ids = list.stream()
                .map(c -> safe(c.id))
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        persistCurrent(list, ids);
    }

    private static void persistCurrent(List<Contract> list, Set<String> ids) throws IOException {
        writeContractsCsv(list);
        PERSISTED_IDS.clear();
        PERSISTED_IDS.addAll(ids);
        dirty = false;
    }

    private static Map<String, Object> toStoreRow(Contract c) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(DataStore.LoanFields.ID, safe(c.id));
        row.put(DataStore.LoanFields.STATUS, safe(c.status));
        row.put(DataStore.LoanFields.NAME, safe(c.name));
        row.put(DataStore.LoanFields.AMOUNT, normalizeAmount(c.money));
        row.put(DataStore.LoanFields.PHONE, safe(c.phone));
        row.put(DataStore.LoanFields.BORROW_DATE, safe(c.vayDate));
        row.put(DataStore.LoanFields.DUE_DATE, safe(c.traDate));
        row.put(DataStore.LoanFields.INTEREST, safe(c.interest));
        row.put(DataStore.LoanFields.TYPE, safe(c.type));
        row.put(DataStore.LoanFields.NOTE, safe(c.note));
        row.put(DataStore.LoanFields.CREATED_AT, safe(c.createdAt));
        return row;
    }

    private static Contract fromStoreRow(Map<String, Object> row) {
        Contract c = new Contract();
        c.id        = safeObject(row.get(DataStore.LoanFields.ID));
        c.status    = safeObject(row.get(DataStore.LoanFields.STATUS));
        c.name      = safeObject(row.get(DataStore.LoanFields.NAME));
        c.money     = appendCurrency(safeObject(row.get(DataStore.LoanFields.AMOUNT)));
        c.phone     = safeObject(row.get(DataStore.LoanFields.PHONE));
        c.vayDate   = safeObject(row.get(DataStore.LoanFields.BORROW_DATE));
        c.traDate   = safeObject(row.get(DataStore.LoanFields.DUE_DATE));
        c.interest  = safeObject(row.get(DataStore.LoanFields.INTEREST));
        c.type      = safeObject(row.get(DataStore.LoanFields.TYPE));
        c.note      = safeObject(row.get(DataStore.LoanFields.NOTE));
        c.createdAt = safeObject(row.get(DataStore.LoanFields.CREATED_AT));
        return c;
    }

    private static void writeContractsCsv(List<Contract> list) throws IOException {
        Files.createDirectories(SAVE_PATH.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(
                SAVE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write(HEADER);
            w.newLine();
            for (Contract c : list) {
                w.write(c.toCsvLine());
                w.newLine();
            }
        }
    }

    private static List<Contract> readAllFromCsv() throws IOException {
        List<Contract> list = new ArrayList<>();
        if (!Files.exists(SAVE_PATH)) return list;

        try (BufferedReader r = Files.newBufferedReader(SAVE_PATH, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            while ((line = r.readLine()) != null) {
                line = stripBOM(line);
                if (line == null) continue;
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] cols = parseCsv(line);
                if (cols.length < 11) continue;

                Contract c = new Contract();
                c.id        = unesc(cols[0]);
                c.status    = unesc(cols[1]);
                c.name      = unesc(cols[2]);
                c.money     = unesc(cols[3]);
                c.phone     = unesc(cols[4]);
                c.vayDate   = unesc(cols[5]);
                c.traDate   = unesc(cols[6]);
                c.interest  = cols[7];
                c.type      = cols[8];
                c.note      = unesc(cols[9]);
                c.createdAt = unesc(cols[10]);
                list.add(c);
            }
        }
        return list;
    }

    private static String normalizeAmount(String money) {
        if (money == null) return "";
        String cleaned = money
                .replace("VND", "")
                .replace("vnd", "")
                .replace("đ", "")
                .replace("Đ", "")
                .trim();
        cleaned = cleaned.replace(" ", "");
        cleaned = cleaned.replace(",", "");
        int firstDot = cleaned.indexOf('.');
        if (firstDot != -1 && cleaned.indexOf('.', firstDot + 1) != -1) {
            cleaned = cleaned.replace(".", "");
        }
        cleaned = cleaned.replaceAll("[^0-9.\\-]", "");
        if (cleaned.isEmpty()) return "";
        try {
            BigDecimal value = new BigDecimal(cleaned);
            return value.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return cleaned;
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String safeObject(Object value) {
        return safe(value == null ? null : value.toString());
    }

    // Tách CSV: tôn trọng dấu ngoặc kép, xử lý "" bên trong
    private static String[] parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false; // đang trong "..."
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Nếu là "" bên trong chuỗi -> thêm 1 dấu " và bỏ qua ký tự kế
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQ = !inQ;
                    sb.append('"');
                }
            } else if (c == '/' && !inQ) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    // Loại BOM (nếu file có BOM ở đầu)
    private static String stripBOM(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    // Bỏ dấu " ... " và chuyển "" -> "
    private static String unesc(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

}
