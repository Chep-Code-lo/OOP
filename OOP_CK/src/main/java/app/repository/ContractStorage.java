package app.repository;

import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Lưu trữ hợp đồng vay/cho vay dưới dạng CSV kết hợp DataStore trong bộ nhớ. */
public class ContractStorage {
    private static final Path SAVE_PATH    = Paths.get("data", "contracts.csv");
    private static final Path COUNTER_PATH = Paths.get("data", "id_counters.properties");
    private static final int  ID_WIDTH     = 4; // -> Co0001, Ch0001 (đổi 5 nếu muốn Co00001)
    private static final String HEADER = "id/status/accountId/accountName/name/money/phone/vayDate/traDate/interest/typeInterest/note/createdAt";
    private static final Set<String> PERSISTED_IDS = new LinkedHashSet<>();
    private static boolean initialized = false;
    private static boolean dirty = false;

    // ====== API TẠO MỚI (giữ nguyên hành vi cũ) ======
    /** Tạo mới một hợp đồng vay/cho vay và ghi nhận vào DataStore (sẽ lưu ra file khi người dùng chọn). */
    public static synchronized void saveBorrow(String status,
                                               String accountId,
                                               String accountName,
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
                accountId,
                accountName,
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
    }

    // ====== Sinh ID tuần tự theo stats (giữ nguyên) ======
    /** Sinh ID mới cho hợp đồng dựa trên trạng thái (Co/Ch + số thứ tự). */
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

    /** Lấy tiền tố ID tương ứng với trạng thái hợp đồng. */
    private static String statusPrefix(String status) {
        if (status == null) return "Co";
        switch (status) {
            case "CoNo": return "Co";
            case "ChNo": return "Ch";
            default:     return status.substring(0, Math.min(2, status.length()));
        }
    }

    /** Escape chuỗi với dấu ngoặc kép để ghi CSV an toàn. */
    private static String esc(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    // =================== BỔ SUNG: DTO & UPDATE ===================

    /** DTO đại diện một dòng hợp đồng trong CSV / DataStore. */
    public static class Contract {
        public String id;
        public String status;
        public String accountId;
        public String accountName;
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

        public Contract(String id, String status, String accountId, String accountName,
                        String name, String money, String phone,
                        String vayDate,String traDate, String interest, String type, String note, String createdAt) {
            this.id = id;
            this.status = status;
            this.accountId = accountId;
            this.accountName = accountName;
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
                    esc(id), esc(status), esc(accountId), esc(accountName), esc(name), esc(money), esc(phone),
                    esc(vayDate),esc(traDate), interest, esc(type), esc(note), esc(createdAt)
            );
        }
    }

    // Trong ContractStorage.java

    /** Đọc toàn bộ hợp đồng vào bộ nhớ (kết hợp DataStore) và trả về danh sách DTO. */
    public static synchronized List<Contract> loadAll() throws IOException {
        ensureLoaded();
        return currentContracts();
    }

    /** Alias giữ tương thích với code cũ, trả về danh sách hợp đồng hiện có. */
    public static synchronized List<Contract> Makecell() throws IOException {
        ensureLoaded();
        return currentContracts();
    }
    /** Ghi đè toàn bộ danh sách hợp đồng và đồng bộ vào DataStore, chờ người dùng lưu ra file. */
    public static synchronized void saveAll(List<Contract> list) throws IOException {
        ensureLoaded();
        DataStore.replaceLoans(list.stream()
                .map(ContractStorage::toStoreRow)
                .collect(Collectors.toList()));
        dirty = true;
    }

    /** Xả những hợp đồng mới/đã sửa từ bộ nhớ xuống CSV khi người dùng yêu cầu, trả về số ID mới thêm. */
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
    /** Cập nhật hợp đồng theo vị trí hiển thị (1-based). */
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
        return true;
    }

    /** Chuẩn hoá chuỗi số tiền về định dạng kèm hậu tố VND. */
    private static String appendCurrency(String money) {
        if (money == null) return "";
        String trimmed = money.trim();
        if (trimmed.isEmpty()) return "";
        if (trimmed.toUpperCase(Locale.ROOT).endsWith("VND")) {
            return trimmed;
        }
        return trimmed + " VND";
    }

    /** Đảm bảo dữ liệu CSV đã được nạp vào bộ nhớ và DataStore. */
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

    /** Chuyển dữ liệu từ DataStore.loans() thành danh sách DTO. */
    private static List<Contract> currentContracts() {
        List<Contract> list = new ArrayList<>();
        for (Map<String, Object> row : DataStore.loans()) {
            list.add(fromStoreRow(row));
        }
        return list;
    }

    /** Ghi danh sách hiện tại ra CSV và cập nhật cache ID đã lưu. */
    private static void persistCurrent() throws IOException {
        List<Contract> list = currentContracts();
        Set<String> ids = list.stream()
                .map(c -> safe(c.id))
                .filter(id -> !id.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        persistCurrent(list, ids);
    }

    /** Ghi CSV và cập nhật danh sách ID đã lưu thành công. */
    private static void persistCurrent(List<Contract> list, Set<String> ids) throws IOException {
        writeContractsCsv(list);
        PERSISTED_IDS.clear();
        PERSISTED_IDS.addAll(ids);
        dirty = false;
    }

    /** Chuyển DTO hợp đồng sang bản ghi Map cho DataStore. */
    private static Map<String, Object> toStoreRow(Contract c) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(DataStore.LoanFields.ID, safe(c.id));
        row.put(DataStore.LoanFields.STATUS, safe(c.status));
        row.put(DataStore.LoanFields.ACCOUNT_ID, safe(c.accountId));
        row.put(DataStore.LoanFields.ACCOUNT_NAME, safe(c.accountName));
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

    /** Chuyển bản ghi Map trong DataStore về DTO. */
    private static Contract fromStoreRow(Map<String, Object> row) {
        Contract c = new Contract();
        c.id        = safeObject(row.get(DataStore.LoanFields.ID));
        c.status    = safeObject(row.get(DataStore.LoanFields.STATUS));
        c.accountId   = safeObject(row.get(DataStore.LoanFields.ACCOUNT_ID));
        c.accountName = safeObject(row.get(DataStore.LoanFields.ACCOUNT_NAME));
        c.name        = safeObject(row.get(DataStore.LoanFields.NAME));
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

    /** Ghi danh sách hợp đồng ra file CSV chính. */
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

    /** Đọc file CSV hiện có và chuyển thành danh sách DTO. */
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

                int idx = 2;
                if (cols.length >= 13) {
                    c.accountId   = unesc(cols[idx++]);
                    c.accountName = unesc(cols[idx++]);
                } else {
                    // tương thích dữ liệu cũ (chưa lưu tài khoản)
                    c.accountId = "";
                    c.accountName = "";
                }
                c.name      = unesc(cols[idx++]);
                c.money     = unesc(cols[idx++]);
                c.phone     = unesc(cols[idx++]);
                c.vayDate   = unesc(cols[idx++]);
                c.traDate   = unesc(cols[idx++]);
                c.interest  = cols[idx++];
                c.type      = cols[idx++];
                c.note      = unesc(cols[idx++]);
                c.createdAt = unesc(cols[idx]);
                list.add(c);
            }
        }
        return list;
    }

    /** Chuẩn hoá chuỗi số tiền về dạng số thuần (không ký tự lạ). */
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

    /** Trả về chuỗi rỗng nếu null. */
    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    /** Chuyển object bất kỳ sang chuỗi an toàn (không null). */
    private static String safeObject(Object value) {
        return safe(value == null ? null : value.toString());
    }

    // Tách CSV: tôn trọng dấu ngoặc kép, xử lý "" bên trong
    /** Tách một dòng CSV với dấu phân cách '/' và hỗ trợ ngoặc kép. */
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
    /** Loại bỏ BOM Unicode ở đầu dòng (nếu có). */
    private static String stripBOM(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    // Bỏ dấu " ... " và chuyển "" -> "
    /** Gỡ bỏ ngoặc kép và unescape "" -> ". */
    private static String unesc(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

}
