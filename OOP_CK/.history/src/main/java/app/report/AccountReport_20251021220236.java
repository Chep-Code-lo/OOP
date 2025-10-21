package app.report;

import app.model.DateRange;
import app.repository.DataStore;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Báo cáo theo tài khoản:
 * - Tổng hợp: Tổng THU, Tổng CHI, Lãi ròng, #giao dịch, ngày gần nhất, số dư hiện tại.
 * - Lọc: Theo khoảng ngày + theo ID/Name (contains).
 * - Nếu chỉ 1 tài khoản -> theo dõi theo chu kỳ D/W/M/Y: Net = Thu - Chi theo bucket.
 * - HỖ TRỢ CHUYỂN KHOẢN NỘI BỘ:
 *      TRANSFER_IN  => Income (tài khoản nhận)
 *      TRANSFER_OUT => Expense (tài khoản gửi)
 * - Thêm DEBUG: đếm số giao dịch đọc được, số trong khoảng ngày, liệt kê các TYPE thô đang có.
 */
public class AccountReport {

    /* ======== Cấu trúc dữ liệu dùng nội bộ ======== */
    public record AccountRow(String id, String name, String type, double balance, String note) {}
    public record TxRow(LocalDate date, String type, double amount, String accountId, String accountName, String category) {}

    private static final Scanner SC = new Scanner(System.in);

    /* =================== API =================== */

    public static void run() {
        System.out.println("============= BÁO CÁO THEO TÀI KHOẢN =============");

        // 1) Lọc theo thời gian
        DateRange range = askDateRange();

        // 2) Lọc theo tài khoản (ID hoặc tên)
        String accFilter = askAccountFilter();

        // 3) Chu kỳ nếu cần theo dõi chi tiết 1 account
        DateRange.Granularity g = askGranularity();

        // 4) Load dữ liệu
        List<AccountRow> accounts = loadAccounts();
        LoadTxResult txRes = loadTransactions(range);   // -> có debug count + typeSet

        // DEBUG: in tình hình đọc giao dịch
        System.out.println();
        System.out.println("---- DEBUG TRANSACTIONS ----");
        System.out.println("Tổng giao dịch đọc được (DataStore.transactions): " + txRes.rawCount);
        System.out.println("Trong khoảng ngày đã chọn: " + txRes.inRangeCount);
        System.out.println("Các TYPE thô gặp phải: " + (txRes.rawTypes.isEmpty() ? "(none)" : String.join(", ", txRes.rawTypes)));
        System.out.println("-----------------------------");
        System.out.println();

        // 5) Áp bộ lọc theo ID/Name (contains)
        List<AccountRow> filtered = filterAccounts(accounts, accFilter);

        // 6) Tổng hợp theo từng account
        printAccountSummary(filtered, txRes.rows);

        // 7) Nếu chỉ 1 account -> theo chu kỳ
        if (filtered.size() == 1) {
            System.out.println();
            printAccountTimeSeries(filtered.get(0), txRes.rows, g);
        }

        pause();
    }

    /* =================== Tổng hợp chính =================== */

    private static void printAccountSummary(List<AccountRow> accounts, List<TxRow> txs) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Map accountId -> list tx
        Map<String, List<TxRow>> txByAcc = txs.stream()
                .collect(Collectors.groupingBy(TxRow::accountId));

        System.out.println();
        System.out.println("======== TỔNG HỢP THEO TÀI KHOẢN ========");
        System.out.printf("%-10s | %-22s | %-10s | %12s | %12s | %12s | %6s | %-10s%n",
                "ID", "Name", "Type", "Tổng THU", "Tổng CHI", "Lãi ròng", "#Tx", "Gần nhất");
        System.out.println("------------------------------------------------------------------------------------------------");

        double sumBalance = 0, sumIncome = 0, sumExpense = 0;

        for (AccountRow a : accounts) {
            List<TxRow> list = txByAcc.getOrDefault(a.id(), Collections.emptyList());
            double income = list.stream().filter(t -> eq(t.type, "Income")).mapToDouble(TxRow::amount).sum();
            double expense = list.stream().filter(t -> eq(t.type, "Expense")).mapToDouble(TxRow::amount).sum();
            double net = income - expense;
            int txCount = list.size();
            LocalDate latest = list.stream().map(TxRow::date).max(LocalDate::compareTo).orElse(null);

            System.out.printf("%-10s | %-22s | %-10s | %12s | %12s | %12s | %6d | %-10s%n",
                    trunc(a.id(),10), trunc(a.name(),22), trunc(nz(a.type()),10),
                    nf.format(Math.round(income)),
                    nf.format(Math.round(expense)),
                    nf.format(Math.round(net)),
                    txCount,
                    (latest==null? "-" : latest.toString()));

            sumBalance += a.balance();
            sumIncome  += income;
            sumExpense += expense;
        }

        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.printf("%-45s | %12s | %12s | %12s |%n",
                "TỔNG", nf.format(Math.round(sumIncome)),
                nf.format(Math.round(sumExpense)), nf.format(Math.round(sumIncome - sumExpense)));
        System.out.printf("%-45s   %s%n", "Tổng số dư hiện tại (từ DataStore.accounts):",
                nf.format(Math.round(sumBalance)));
    }

    /** Theo dõi 1 tài khoản theo chu kỳ (D/W/M/Y): Net = Thu - Chi theo bucket. */
    private static void printAccountTimeSeries(AccountRow acc, List<TxRow> txs, DateRange.Granularity g) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        List<TxRow> list = txs.stream()
                .filter(t -> Objects.equals(t.accountId(), acc.id()))
                .toList();

        if (list.isEmpty()) {
            System.out.println("(*) Không có giao dịch nào cho tài khoản \""+acc.name()+"\" trong khoảng đã chọn.");
            return;
        }

        Map<String, Double> byBucket = list.stream()
                .collect(Collectors.groupingBy(
                        t -> DateRange.bucketKey(t.date(), g),
                        TreeMap::new,
                        Collectors.summingDouble(t ->
                                eq(t.type(), "Expense") ? -t.amount() : t.amount())
                ));

        System.out.println("======== THEO DÕI TÀI KHOẢN: " + acc.name() + " ("+acc.id()+") ========");
        System.out.println("Chu kỳ: " + g);
        System.out.printf("%-10s | %12s%n", "Bucket", "Net (₫)");
        System.out.println("------------------------------");
        byBucket.forEach((k,v) ->
                System.out.printf("%-10s | %12s%n", k, nf.format(Math.round(v))));
    }

    /* =================== Load dữ liệu =================== */

    private static List<AccountRow> loadAccounts() {
        List<Map<String, Object>> raw = DataStore.accounts();
        List<AccountRow> rows = new ArrayList<>();
        for (Map<String, Object> r : raw) {
            String id   = s(r, DataStore.AccountFields.ID);
            String name = s(r, DataStore.AccountFields.NAME);
            String type = s(r, DataStore.AccountFields.TYPE);
            String note = s(r, DataStore.AccountFields.NOTE);
            double balance = parseDoubleSafe(s(r, DataStore.AccountFields.BALANCE),
                    r.get(DataStore.AccountFields.BALANCE));
            rows.add(new AccountRow(nz(id), nz(name), nz(type), balance, nz(note)));
        }
        rows.sort(Comparator.comparing(AccountRow::name, String.CASE_INSENSITIVE_ORDER));
        return rows;
    }

    /** Kết quả load giao dịch kèm debug */
    private record LoadTxResult(List<TxRow> rows, int rawCount, int inRangeCount, Set<String> rawTypes) {}

    private static LoadTxResult loadTransactions(DateRange range) {
        List<Map<String, Object>> raw = DataStore.transactions();
        int rawCount = (raw == null ? 0 : raw.size());
        int inRangeCount = 0;

        // Thu thập type thô để debug
        Set<String> rawTypes = new LinkedHashSet<>();

        List<TxRow> list = new ArrayList<>();
        if (raw != null) {
            for (Map<String, Object> r : raw) {
                String date  = s(r, DataStore.TransactionFields.DATE);
                String type0 = s(r, DataStore.TransactionFields.TYPE); // giữ lại để debug
                rawTypes.add(type0);

                String type  = normalizeTxType(type0); // chuẩn hoá sang Income/Expense (gồm cả chuyển khoản)
                String amtS  = s(r, DataStore.TransactionFields.AMOUNT);
                String accId = s(r, DataStore.TransactionFields.ACCOUNT_ID);
                String accNm = s(r, DataStore.TransactionFields.ACCOUNT_NAME);
                String cat   = s(r, DataStore.TransactionFields.CATEGORY);

                LocalDate d = parseDate(date);
                if (d == null) continue;
                if (d.isBefore(range.start()) || d.isAfter(range.end())) continue;

                inRangeCount++;
                double amount = parseDoubleSafe(amtS, r.get(DataStore.TransactionFields.AMOUNT));
                list.add(new TxRow(d, type, amount, nz(accId), nz(accNm), nz(cat)));
            }
        }
        return new LoadTxResult(list, rawCount, inRangeCount, rawTypes);
    }

    /**
     * Chuẩn hoá TYPE về "Income"/"Expense" để dễ tổng hợp:
     * - income, in => Income
     * - expense, ex => Expense
     * - transfer_in / transfer-in / transfer in / transferin => Income
     * - transfer_out / transfer-out / transfer out / transferout => Expense
     * (bỏ hết ký tự không phải a-z để bắt nhiều biến thể)
     */
    private static String normalizeTxType(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if (s.equals("income") || s.equals("in")) return "Income";
        if (s.equals("expense") || s.equals("ex")) return "Expense";
        if (s.equals("transferin"))  return "Income";
        if (s.equals("transferout")) return "Expense";
        // fallback: nếu raw đã là "Income"/"Expense"
        if ("income".equalsIgnoreCase(raw))  return "Income";
        if ("expense".equalsIgnoreCase(raw)) return "Expense";
        return raw;
    }

    /* =================== Hỏi đầu vào =================== */

    /** Cho nhập nhiều định dạng ngày: yyyy-MM-dd, dd-MM-yyyy, dd/MM/yyyy */
    private static DateRange askDateRange(){
        System.out.print("Từ ngày (yyyy-MM-dd | dd-MM-yyyy | dd/MM/yyyy, Enter=all): ");
        String s = SC.nextLine().trim();
        System.out.print("Đến ngày (yyyy-MM-dd | dd-MM-yyyy | dd/MM/yyyy, Enter=all): ");
        String e = SC.nextLine().trim();
        if (s.isEmpty() || e.isEmpty()) return new DateRange(LocalDate.MIN, LocalDate.MAX);
        LocalDate start = parseDate(s);
        LocalDate end   = parseDate(e);
        if (start == null || end == null) {
            System.out.println("Cảnh báo: Ngày không hợp lệ → dùng All time.");
            return new DateRange(LocalDate.MIN, LocalDate.MAX);
        }
        return new DateRange(start, end);
    }

    /** Nhập chuỗi lọc ID/Name (contains). */
    private static String askAccountFilter() {
        System.out.print("Lọc theo tài khoản (nhập ID hoặc tên, Enter = tất cả): ");
        return SC.nextLine().trim();
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

    /* =================== Helpers =================== */

    private static List<AccountRow> filterAccounts(List<AccountRow> accounts, String keyword) {
        if (keyword == null || keyword.isBlank()) return accounts;
        String kw = keyword.toLowerCase(Locale.ROOT).trim();
        return accounts.stream()
                .filter(a -> a.id().toLowerCase(Locale.ROOT).contains(kw)
                        || a.name().toLowerCase(Locale.ROOT).contains(kw))
                .toList();
    }

    private static String s(Map<String,Object> r, String key){
        Object v = (r==null)? null : r.get(key);
        return v==null? "" : String.valueOf(v);
    }
    private static String nz(String s){ return s==null? "" : s; }
    private static boolean eq(String a, String b){ return a!=null && a.equalsIgnoreCase(b); }
    private static String trunc(String s, int max){ return (s==null||s.length()<=max)? nz(s) : s.substring(0,max-1)+"…"; }
    private static double parseDoubleSafe(String s, Object number){
        try {
            if (number instanceof Number n) return n.doubleValue();
            return Double.parseDouble(s.replace(",","").trim());
        } catch (Exception e){ return 0.0; }
    }

    // Hỗ trợ nhiều định dạng ngày phổ biến
    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DMY_DASH = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DMY_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static LocalDate parseDate(String s){
        if (s==null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        for (DateTimeFormatter f : new DateTimeFormatter[]{ISO, DMY_DASH, DMY_SLASH}) {
            try { return LocalDate.parse(t, f); } catch (Exception ignore) {}
        }
        return null;
    }

    private static void pause() {
        System.out.print("Nhấn Enter để tiếp tục...");
        try {
            System.in.read();
            if (System.in.available() > 0) System.in.skip(System.in.available());
        } catch (Exception ignored) {}
    }
}
