package app.report;

import app.model.DateRange;
import app.repository.DataStore;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class IncomeExpenseReport {

    public enum TxClass { ALL, INCOME, EXPENSE }
    private static final Scanner SC = new Scanner(System.in);

    /* ===================== API CHO MenuPayment ===================== */

    /** Overload cho MenuPayment: đã có range, categories, txClass */
    public static void run(DateRange range, List<String> categories, TxClass txClass) {
        DateRange.Granularity g = askGranularity();
        runInternal(range, categories, txClass, g);
    }

    /** In ra danh mục đang có & cho người dùng chọn (số hoặc tên). */
    public static List<String> promptCategories() {
        List<String> avail = availableCategories();
        if (avail.isEmpty()) {
            System.out.println("Không tìm thấy danh mục nào trong dữ liệu. (Enter = tất cả)");
            System.out.print("Danh mục (tên hoặc số, cách nhau dấu phẩy, Enter=tất cả): ");
            String raw = SC.nextLine().trim();
            return raw.isBlank() ? Collections.emptyList() : normalizeNameList(Arrays.asList(raw.split(",")));
        }

        System.out.println("Danh mục hiện có:");
        for (int i = 0; i < avail.size(); i++) {
            System.out.printf("  %2d) %s%n", i + 1, avail.get(i));
        }
        System.out.print("Chọn bằng số (vd: 1,3,5) hoặc gõ tên (vd: Ăn uống, Xăng xe); Enter=tất cả: ");
        String raw = SC.nextLine().trim();
        if (raw.isBlank()) return Collections.emptyList(); // tất cả

        // Tách phần tử
        String[] parts = raw.split(",");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (s.isEmpty()) continue;

            // Nếu là số: map sang danh mục theo chỉ số
            if (s.chars().allMatch(Character::isDigit)) {
                try {
                    int idx = Integer.parseInt(s);
                    if (1 <= idx && idx <= avail.size()) {
                        out.add(avail.get(idx - 1));
                    }
                } catch (NumberFormatException ignore) {}
            } else { // nếu là tên: khớp theo equalsIgnoreCase trước, nếu không có thì contains
                Optional<String> exact = avail.stream().filter(a -> a.equalsIgnoreCase(s)).findFirst();
                if (exact.isPresent()) {
                    out.add(exact.get());
                } else {
                    List<String> contains = avail.stream()
                            .filter(a -> a.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)))
                            .toList();
                    out.addAll(contains);
                }
            }
        }
        // bỏ trùng & giữ thứ tự xuất hiện gốc
        LinkedHashSet<String> set = new LinkedHashSet<>(out);
        return new ArrayList<>(set);
    }

    /** Trả về danh sách danh mục duy nhất (đã sort) lấy từ DataStore. */
    public static List<String> availableCategories() {
        List<Map<String, Object>> rows = DataStore.transactions();
        LinkedHashSet<String> cats = new LinkedHashSet<>();
        for (Map<String, Object> r : rows) {
            Object v = r.get("CATEGORY");
            if (v == null) v = r.get("category");
            if (v == null) v = r.get("Cat");
            String s = (v == null) ? "" : String.valueOf(v).trim();
            if (!s.isEmpty()) cats.add(s);
        }
        List<String> sorted = new ArrayList<>(cats);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    /* ===================== CHẠY TỰ DO (CLI cũ) ===================== */

    public static void run() {
        DateRange range = askDateRange();
        TxClass txClass = askTxClass();

        // Hiển thị & chọn danh mục từ dữ liệu
        List<String> cats = promptCategories();

        DateRange.Granularity g = askGranularity();
        runInternal(range, cats, txClass, g);
    }

    private static void runInternal(DateRange range, List<String> categories, TxClass txClass,
                                    DateRange.Granularity g) {
        List<Map<String,Object>> rows = DataStore.transactions();

        final List<String> normCats = (categories == null) ? Collections.emptyList()
                : categories.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT).trim())
                .toList();

        List<TxRow> txs = new ArrayList<>();
        for (Map<String,Object> r: rows) {
            String type  = str(r, "TYPE","type","TxType");
            String cat   = str(r, "CATEGORY","category","Cat");
            String dateS = str(r, "DATE","date");
            String amtS  = str(r, "AMOUNT","amount","Money");

            LocalDate d;
            try { d = LocalDate.parse(dateS); } catch (Exception e) { continue; }
            if (d.isBefore(range.start()) || d.isAfter(range.end())) continue;

            // Nhận diện loại: gộp cả chuyển khoản
            boolean isIncomeType  = eq(type,"Income")  || eq(type,"TRANSFER_IN");
            boolean isExpenseType = eq(type,"Expense") || eq(type,"TRANSFER_OUT");

            if (txClass == TxClass.INCOME && !isIncomeType)  continue;
            if (txClass == TxClass.EXPENSE && !isExpenseType) continue;

            // Lọc theo danh mục: rỗng = không lọc; có "*" = không lọc.
            boolean matchCategory = true;
            if (!normCats.isEmpty() && !normCats.contains("*")) {
                String c = (cat == null ? "" : cat.toLowerCase(Locale.ROOT));
                matchCategory = normCats.stream().anyMatch(k -> c.equals(k) || c.contains(k));
            }
            if (!matchCategory) continue;

            double amount = parseDouble(amtS);

            // Nếu là chuyển khoản mà không có category -> gán tên dễ đọc
            if (eq(type,"TRANSFER_IN")  && (cat == null || cat.isBlank())) cat = "Chuyển khoản đến";
            if (eq(type,"TRANSFER_OUT") && (cat == null || cat.isBlank())) cat = "Chuyển khoản đi";

            // Lưu bản ghi đã normalize
            txs.add(new TxRow(d, nz(cat), isIncomeType ? "Income" : (isExpenseType ? "Expense" : nz(type)), amount));
        }

        // Tổng hợp theo danh mục (Expense âm, Income dương)
        Map<String, Double> byCategory = new LinkedHashMap<>();
        for (TxRow t: txs) {
            double signed = "Expense".equalsIgnoreCase(t.type) ? -t.amount : t.amount;
            byCategory.merge(t.category, signed, Double::sum);
        }

        // Tổng hợp theo chu kỳ (Expense âm, Income dương)
        Map<String, Double> byBucket = txs.stream()
                .collect(Collectors.groupingBy(
                        t -> DateRange.bucketKey(t.date, g),
                        TreeMap::new,
                        Collectors.summingDouble(t -> "Expense".equalsIgnoreCase(t.type) ? -t.amount : t.amount)
                ));

        // Tổng thu/chi (đã gộp cả chuyển khoản)
        double totalIncome  = txs.stream()
                .filter(t -> eq(t.type,"Income"))
                .mapToDouble(t -> t.amount).sum();
        double totalExpense = txs.stream()
                .filter(t -> eq(t.type,"Expense"))
                .mapToDouble(t -> t.amount).sum();
        double net = totalIncome - totalExpense;

        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi","VN"));

        System.out.println("============= BÁO CÁO THU - CHI =============");
        System.out.printf("%-28s | %18s%n", "Danh mục", "Tổng (₫)");
        System.out.println("---------------------------------------------");
        for (var e: byCategory.entrySet())
            System.out.printf("%-28s | %18s%n", truncate(e.getKey(), 28), nf.format(Math.round(e.getValue())));
        System.out.println("---------------------------------------------");
        System.out.printf("%-28s | %18s%n", "Tổng thu", nf.format(Math.round(totalIncome)));
        System.out.printf("%-28s | %18s%n", "Tổng chi", nf.format(Math.round(totalExpense)));
        System.out.printf("%-28s | %18s%n", "Lãi ròng", nf.format(Math.round(net)));
        System.out.println();

        System.out.println("======= TỔNG HỢP THEO CHU KỲ ("+g+") =======");
        System.out.printf("%-12s | %18s%n", "Bucket", "Net (₫)");
        System.out.println("---------------------------------------------");
        for (var e: byBucket.entrySet())
            System.out.printf("%-12s | %18s%n", e.getKey(), nf.format(Math.round(e.getValue())));
        System.out.println();
    }

    /* ===================== UTIL ===================== */

    private static class TxRow {
        final LocalDate date; final String category; final String type; final double amount;
        TxRow(LocalDate d, String c, String t, double a){ date=d; category=c; type=t; amount=a; }
    }

    private static String nz(String s){ return s==null? "" : s; }
    private static boolean eq(String a, String b){ return a!=null && a.equalsIgnoreCase(b); }
    private static String truncate(String s, int max){ return (s==null||s.length()<=max)? nz(s) : s.substring(0,max-1) + "…"; }
    private static double parseDouble(String s){ try { return Double.parseDouble(s.replace(",","").trim()); } catch(Exception e){ return 0.0; } }
    private static String str(Map<String,Object> r, String... keys){
        for (String k: keys) if (r.containsKey(k)) return String.valueOf(r.get(k));
        return "";
    }
    private static List<String> normalizeNameList(Collection<String> items){
        return items.stream().map(v -> v==null? "" : v.trim())
                .filter(v -> !v.isEmpty()).toList();
    }

    private static DateRange askDateRange(){
        System.out.print("Nhập ngày bắt đầu (yyyy-MM-dd, Enter=All): ");
        String s = SC.nextLine().trim();
        System.out.print("Nhập ngày kết thúc (yyyy-MM-dd, Enter=All): ");
        String e = SC.nextLine().trim();
        if (s.isEmpty() || e.isEmpty()) return DateRange.allTime();
        try { return new DateRange(LocalDate.parse(s), LocalDate.parse(e)); }
        catch (Exception ex){ System.out.println("Ngày không hợp lệ → dùng All time."); return DateRange.allTime(); }
    }
    private static TxClass askTxClass(){
        System.out.println("Chọn phân loại:");
        System.out.println("1) Tất cả");
        System.out.println("2) Chỉ THU");
        System.out.println("3) Chỉ CHI");
        System.out.print("Chọn: ");
        String s = SC.nextLine().trim();
        return switch (s){
            case "2","I","i" -> TxClass.INCOME;
            case "3","E","e" -> TxClass.EXPENSE;
            default          -> TxClass.ALL;
        };
    }
    private static DateRange.Granularity askGranularity(){
        System.out.print("Chu kỳ gộp (D=Day, W=Week, M=Month, Y=Year): ");
        String s = SC.nextLine().trim().toUpperCase(Locale.ROOT);
        return switch (s){
            case "D" -> DateRange.Granularity.DAY;
            case "W" -> DateRange.Granularity.WEEK;
            case "Y" -> DateRange.Granularity.YEAR;
            default  -> DateRange.Granularity.MONTH;
        };
    }
}
