package app.report;

import app.model.DateRange;
import app.repository.DataStore;

import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LoanReport {

    private static final Scanner SC = new Scanner(System.in);

    public static class LoanRow {
        private final String id;
        private final String name;
        private final double amount;
        private final LocalDate date;     // ngày vay (borrowDate/createdAt)
        private final LocalDate dueDate;  // hạn trả
        private final String status;

        public LoanRow(String id, String name, double amount, LocalDate date, LocalDate dueDate, String status) {
            this.id = nz(id);
            this.name = nz(name);
            this.amount = amount;
            this.date = date;
            this.dueDate = dueDate;
            this.status = nz(status);
        }
        public String id()      { return id; }
        public String name()    { return name; }
        public double amount()  { return amount; }
        public LocalDate dueDate() { return dueDate; }
        public String status()  { return status; }
        public LocalDate date() { return date; } // nội bộ
    }

    /* ===== API cho MenuPayment: nhận range + danh sách trạng thái ===== */
    public static void run(DateRange range, List<String> statuses) {
        DateRange.Granularity g = askGranularity();
        runInternal(range, statuses, g);
    }

    /* ===== Chạy tự do ===== */
    public static void run() {
        DateRange range = askDateRange();
        DateRange.Granularity g = askGranularity();
        runInternal(range, Collections.emptyList(), g);
    }

    /* ===== Core ===== */
    private static void runInternal(DateRange range, List<String> statuses, DateRange.Granularity g) {
        List<Map<String,Object>> raw = DataStore.loans();
        int rawCount = (raw == null ? 0 : raw.size());

        int droppedByDate = 0;
        int droppedByStatus = 0;

        List<LoanRow> loans = new ArrayList<>();
        final List<String> normStatuses = normalizeList(statuses);

        if (raw != null) {
            for (Map<String, Object> r : raw) {
                // Đọc đúng key theo DataStore.LoanFields
                String id      = str(r, DataStore.LoanFields.ID);          // loanId
                String name    = str(r, DataStore.LoanFields.NAME);        // name
                String status  = str(r, DataStore.LoanFields.STATUS);      // status
                String amtS    = str(r, DataStore.LoanFields.AMOUNT);      // amount
                String borrowS = str(r, DataStore.LoanFields.BORROW_DATE); // borrowDate
                String createdS= str(r, DataStore.LoanFields.CREATED_AT);  // createdAt
                String dueS    = str(r, DataStore.LoanFields.DUE_DATE);    // dueDate

                // ngày: ưu tiên borrowDate, rỗng thì dùng createdAt
                LocalDate date = parseDateFlexible(borrowS);
                if (date == null) date = parseDateFlexible(createdS);
                if (date == null) { droppedByDate++; continue; }

                if (date.isBefore(range.start()) || date.isAfter(range.end())) {
                    droppedByDate++; continue;
                }

                // lọc trạng thái (mềm): không phân biệt hoa/thường, bỏ dấu & khoảng trắng, chấp nhận contains
                if (!normStatuses.isEmpty() && !normStatuses.contains("*")) {
                    String stN = normalize(status);
                    boolean ok = normStatuses.stream().anyMatch(s -> stN.contains(s));
                    if (!ok) { droppedByStatus++; continue; }
                }

                LocalDate due = parseDateFlexible(dueS);
                double amount = parseDoubleSafe(amtS, r.get(DataStore.LoanFields.AMOUNT));

                loans.add(new LoanRow(id, name, amount, date, due, status));
            }
        }

        // Debug số lượng
        System.out.println("============== BÁO CÁO KHOẢN VAY/CHO VAY ==============");
        System.out.println("Nguồn: DataStore.loans()");
        System.out.println("Bản ghi đọc được: " + rawCount);
        System.out.println("Bản ghi hợp lệ sau parse (thuộc range & lọc): " + loans.size());
        System.out.println("Loại do ngày (format/ngoài range): " + droppedByDate);
        System.out.println("Loại do trạng thái: " + droppedByStatus);
        System.out.println();

        if (loans.isEmpty()) {
            System.out.println("(Không có dữ liệu khoản vay/cho vay. Kiểm tra lại khoảng ngày hoặc trạng thái lọc.)");
            pause(); return;
        }

        // Tổng hợp THEO CHU KỲ
        Map<String, Double> sumByBucket = loans.stream()
                .collect(Collectors.groupingBy(
                        l -> DateRange.bucketKey(l.date(), g),
                        TreeMap::new,
                        Collectors.summingDouble(LoanRow::amount)
                ));
        double total = sumByBucket.values().stream().mapToDouble(Double::doubleValue).sum();

        // Tổng theo trạng thái
        Map<String, Double> byStatus = loans.stream()
                .collect(Collectors.groupingBy(
                        l -> l.status().isBlank()? "Unknown" : l.status(),
                        TreeMap::new,
                        Collectors.summingDouble(LoanRow::amount)
                ));

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi","VN"));

        System.out.println("===== KHOẢN VAY THEO CHU KỲ ("+g+") =====");
        System.out.printf("%-12s | %18s%n", "Bucket", "Sum (₫)");
        System.out.println("---------------------------------------------");
        sumByBucket.forEach((k,v) -> System.out.printf("%-12s | %18s%n", k, nf.format(Math.round(v))));
        System.out.printf("%-12s | %18s%n%n", "Total", nf.format(Math.round(total)));

        System.out.println("===== TỔNG THEO TRẠNG THÁI =====");
        byStatus.forEach((st,sum) ->
                System.out.printf("%-12s | %18s%n", truncate(st,12), nf.format(Math.round(sum)))
        );
        System.out.println();

        // (Nếu cần export CSV: PaymentReportSaver.saveLoanReport(range, loans, total);)
        pause();
    }

    /* ===== Util ===== */
    private static String str(Map<String,Object> r, String key){
        Object v = (r==null)? null : r.get(key);
        return v==null? "" : String.valueOf(v);
    }
    private static double parseDoubleSafe(String s, Object number){
        try {
            if (number instanceof Number n) return n.doubleValue();
            return Double.parseDouble(s.replace(",","").trim());
        } catch (Exception e){ return 0.0; }
    }

    // NEW: Parse ngày "đa định dạng": yyyy-MM-dd, dd-MM-yyyy, dd/MM/yyyy
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DMY_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DMY_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static LocalDate parseDateFlexible(String s){
        if (s==null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        for (DateTimeFormatter f : new DateTimeFormatter[]{ISO, DMY_DASH, DMY_SLASH}) {
            try { return LocalDate.parse(t, f); } catch (Exception ignore) {}
        }
        return null;
    }

    private static String normalize(String s){
        if (s == null) return "";
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noAccent.toLowerCase(Locale.ROOT).replace(" ", "");
    }
    private static List<String> normalizeList(List<String> in){
        if (in == null) return Collections.emptyList();
        List<String> out = new ArrayList<>();
        for (String s : in) {
            if (s == null) continue;
            s = s.trim();
            if (s.isEmpty()) continue;
            out.add(normalize(s));
        }
        return out;
    }

    private static String nz(String s){ return s==null? "" : s; }
    private static String truncate(String s, int max){ return (s==null||s.length()<=max)? nz(s) : s.substring(0,max-1) + "…"; }

    private static void pause() {
        System.out.print("Nhấn Enter để tiếp tục...");
        try {
            System.in.read();
            if (System.in.available() > 0) System.in.skip(System.in.available());
        } catch (Exception ignored) {}
    }

    // NEW: Cho phép nhập nhiều định dạng ngày
    private static DateRange askDateRange(){
        System.out.print("Từ ngày (yyyy-MM-dd hoặc dd-MM-yyyy hoặc dd/MM/yyyy, Enter = tất cả): ");
        String s = SC.nextLine().trim();
        System.out.print("Đến ngày (yyyy-MM-dd hoặc dd-MM-yyyy hoặc dd/MM/yyyy, Enter = tất cả): ");
        String e = SC.nextLine().trim();
        if (s.isEmpty() || e.isEmpty()) return new DateRange(LocalDate.MIN, LocalDate.MAX);
        LocalDate start = parseDateFlexible(s);
        LocalDate end   = parseDateFlexible(e);
        if (start == null || end == null) {
            System.out.println("Cảnh báo: Ngày không hợp lệ → dùng All time.");
            return new DateRange(LocalDate.MIN, LocalDate.MAX);
        }
        return new DateRange(start, end);
    }

    private static DateRange.Granularity askGranularity(){
        System.out.print("Chu kỳ gộp (D=Day, W=Week, M=Month, Y=Year):  ");
        String s = SC.nextLine().trim().toUpperCase(Locale.ROOT);
        return switch (s){
            case "D" -> DateRange.Granularity.DAY;
            case "W" -> DateRange.Granularity.WEEK;
            case "Y" -> DateRange.Granularity.YEAR;
            default  -> DateRange.Granularity.MONTH;
        };
    }
}
