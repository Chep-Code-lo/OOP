package app.loan;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class ContractStorage {
    private static final Path SAVE_PATH    = Paths.get("data", "contracts.csv");
    private static final Path COUNTER_PATH = Paths.get("data", "id_counters.properties");
    private static final int  ID_WIDTH     = 4; // -> Co0001, Ch0001 (đổi 5 nếu muốn Co00001)

    // ====== API TẠO MỚI (giữ nguyên hành vi cũ) ======
    public static void saveBorrow(String status,
                                  String name,
                                  String money,
                                  String phone,
                                  String vayDate,
                                  String traDate,
                                  double interest,
                                  String type,
                                  String note) throws IOException {
        Files.createDirectories(SAVE_PATH.getParent());

        boolean existed = Files.exists(SAVE_PATH);
        String id = nextIdForStatus(status); // giữ nguyên cơ chế cấp ID cũ

        try (BufferedWriter w = Files.newBufferedWriter(
                SAVE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            if (!existed) {
                w.write("id/status/name/money/phone/vayDate/traDate/interest/typeInterest/note/createdAt");
                w.newLine();
            }

            String createdAt = LocalDateTime.now().toString();

            String line = String.join("/" ,
                    esc(id), esc(status), esc(name), esc(money) + " VND", esc(phone),
                    esc(vayDate),esc(traDate), String.valueOf(interest), esc(type), esc(note), esc(createdAt)
            );
            w.write(line);
            w.newLine();
        }
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
        public String money;    // lưu string vì file đang lưu "1000.0 VND"
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

    public static List<Contract> loadAll() throws IOException {
        List<Contract> list = new ArrayList<>();
        if (!Files.exists(SAVE_PATH)) return list;

        try (BufferedReader r = Files.newBufferedReader(SAVE_PATH, StandardCharsets.UTF_8)) {
            String line;
            boolean isHeader = true;
            while ((line = r.readLine()) != null) {
                if (line == null) continue;
                line = stripBOM(line).trim();
                if (line.isEmpty()) continue;

                // Bỏ header (dòng đầu tiên)
                if (isHeader) { isHeader = false; continue; }

                String[] cols = parseCsv(line);

                Contract c = new Contract();
                c.id        = unesc(cols[0]);
                c.status    = unesc(cols[1]);
                c.name      = unesc(cols[2]);
                c.money     = unesc(cols[3]);           // money đang lưu "123.0 VND" -> giữ nguyên
                c.phone     = unesc(cols[4]);
                c.vayDate   = unesc(cols[5]);
                c.traDate   = unesc(cols[6]);
                c.interest  = cols[7];           // interest là số dạng chuỗi
                c.type      = cols[8];
                c.note      = unesc(cols[9]);
                c.createdAt = unesc(cols[10]);

                list.add(c);
            }
        }
        return list;
    }
    public static List<Contract> Makecell() throws IOException {
        List<Contract> list = new ArrayList<>();
        if (!Files.exists(SAVE_PATH)) return list;

        try (BufferedReader br = Files.newBufferedReader(SAVE_PATH, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            char delim = header.contains("/") ? '/' : ',';
            String regex = Pattern.quote(String.valueOf(delim));

            String[] hcols = header.trim().split(regex, -1);
            Map<String,Integer> pos = new HashMap<>();
            for (int i = 0; i < hcols.length; i++) {
                pos.put(hcols[i].trim().toLowerCase(), i);
            }
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] cols = line.split(regex, -1);

                Contract c = new Contract();
                // ... các cột khác
                c.vayDate = cell(cols, pos, "vaydate");
                c.traDate = cell(cols, pos, "tradate");
                c.type    = firstNonEmpty(
                        cell(cols, pos, "typeinterest"),
                        cell(cols, pos, "type")
                );
                c.interest = cell(cols, pos, "interest");
                list.add(c);
            }
        }
        return list;
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



    /** Ghi đè lại toàn bộ CSV */
    public static void saveAll(List<Contract> list) throws IOException {
        Files.createDirectories(SAVE_PATH.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(
                SAVE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("id/status/name/money/phone/vayDate/traDate/interest/typeInterest/note/createdAt");
            w.newLine();
            for (Contract c : list) {
                w.write(c.toCsvLine());
                w.newLine();
            }
        }
    }

    /** Cập nhật theo số thứ tự (1-based index). Giữ nguyên id/createdAt. */
    public static boolean updateByIndex(int oneBasedIndex, Contract updated) throws IOException {
        List<Contract> list = loadAll();
        if (oneBasedIndex < 1 || oneBasedIndex > list.size()) return false;
        int idx = oneBasedIndex - 1;
        Contract old = list.get(idx);

        // Giữ id và createdAt cũ
        updated.id = old.id;
        updated.createdAt = old.createdAt;

        list.set(idx, updated);
        saveAll(list);
        return true;
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