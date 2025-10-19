package app.payment;

import app.store.DataStore;
import app.util.ConsoleUtils;
import app.menu.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class PaymentAction {
    private static final Scanner sc = new Scanner(System.in);
    // ===== Thu - Chi: nhập bộ lọc rồi chạy
    public static void runIncomeExpenseWithFilters() {
        ConsoleUtils.clear();
        System.out.println("===== BÁO CÁO THU - CHI (có lọc) =====");

        DateRange range = inputDateRange();
        List<String> categories = inputCategories(); // có thể rỗng
        IncomeExpenseReport.TxClass type = MenuIncomeExpense.inputTxClass(); // Tất cả / Thu / Chi

        logIncomeExpenseSelection(range, categories, type);
        IncomeExpenseReport.run(range, categories, type);

    }

    // ===== Khoản vay: lọc theo hạn trả + Trạng thái
    public static void runLoanWithFilters() {
        ConsoleUtils.clear();
        System.out.println("===== BÁO CÁO KHOẢN VAY (lọc hạn trả + trạng thái) =====");

        DateRange range = inputDateRange();
        List<String> statuses = inputLoanStatuses(); // có thể rỗng

        logLoanSelection(range, statuses);
        LoanReport.run(range, statuses);
    }

    // === Helpers nhập liệu chung ===
    public static DateRange inputDateRange() {
        System.out.println("Nhập khoảng ngày theo định dạng yyyy-MM-dd (Enter để bỏ qua):");
        LocalDate start = readDateOrNull("Từ ngày: ");
        LocalDate end   = readDateOrNull("Đến ngày: ");

        if (start == null && end == null) return DateRange.allTime();
        if (start == null) start = LocalDate.MIN;
        if (end   == null) end   = LocalDate.MAX;
        if (end.isBefore(start)) {
            System.out.println("⚠ 'Đến ngày' < 'Từ ngày' → tự động hoán đổi.");
            LocalDate tmp = start; start = end; end = tmp;
        }
        return new DateRange(start, end);
    }

    public static LocalDate readDateOrNull(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return LocalDate.parse(s); // yyyy-MM-dd
            } catch (DateTimeParseException e) {
                System.out.println("Không đúng định dạng (yyyy-MM-dd). Nhập lại hoặc Enter để bỏ qua.");
            }
        }
    }

    /** Lấy danh mục từ dữ liệu hiện có và cho phép chọn nhiều */
    public static List<String> inputCategories() {
        // Lấy danh mục hiện có từ DataStore
        Set<String> set = DataStore.transactions().stream()
                .map(m -> String.valueOf(m.get(DataStore.TransactionFields.CATEGORY)))
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toCollection(TreeSet::new)); // sort asc

        List<String> list = new ArrayList<>(set);
        if (list.isEmpty()) {
            System.out.println("Hiện chưa có danh mục nào trong dữ liệu. Bỏ qua lọc danh mục.");
            return List.of();
        }

        System.out.println("Chọn danh mục (nhập số, cách nhau dấu phẩy; Enter để lấy TẤT CẢ):");
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, list.get(i));
        }
        System.out.print("Chọn: ");
        String raw = sc.nextLine().trim();
        if (raw.isEmpty()) return List.of(); // không lọc

        Set<Integer> picks = new HashSet<>();
        for (String tok : raw.split(",")) {
            try {
                int idx = Integer.parseInt(tok.trim());
                if (1 <= idx && idx <= list.size()) picks.add(idx - 1);
            } catch (NumberFormatException ignored) {}
        }
        if (picks.isEmpty()) {
            System.out.println("Không chọn hợp lệ → bỏ qua lọc danh mục.");
            return List.of();
        }
        return picks.stream().sorted().map(list::get).toList();
    }

    /** Chọn phân loại Thu/Chi */
    

    /** Chọn trạng thái khoản vay (đa chọn) */
    private static List<String> inputLoanStatuses() {
        // Lấy danh sách trạng thái hiện có từ DataStore
        Set<String> set = DataStore.loans().stream()
                .map(m -> String.valueOf(m.get(DataStore.LoanFields.STATUS)))
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toCollection(TreeSet::new));

        List<String> list = new ArrayList<>(set);
        if (list.isEmpty()) {
            System.out.println("Hiện chưa có trạng thái nào trong dữ liệu. Bỏ qua lọc trạng thái.");
            return List.of();
        }

        System.out.println("Chọn trạng thái (nhập số, cách nhau dấu phẩy; Enter để lấy TẤT CẢ):");
        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%2d) %s%n", i + 1, list.get(i));
        }
        System.out.print("Chọn: ");
        String raw = sc.nextLine().trim();
        if (raw.isEmpty()) return List.of(); // không lọc

        Set<Integer> picks = new HashSet<>();
        for (String tok : raw.split(",")) {
            try {
                int idx = Integer.parseInt(tok.trim());
                if (1 <= idx && idx <= list.size()) picks.add(idx - 1);
            } catch (NumberFormatException ignored) {}
        }
        if (picks.isEmpty()) {
            System.out.println("Không chọn hợp lệ → bỏ qua lọc trạng thái.");
            return List.of();
        }
        return picks.stream().sorted().map(list::get).toList();
    }

    private static void logIncomeExpenseSelection(DateRange range, List<String> categories, IncomeExpenseReport.TxClass type) {
        String rangeText = ReportUtils.describeRange(range);
        String categoryText = ReportUtils.describeSelection(categories, "Tất cả danh mục");
        String typeText = type == null ? "Không xác định" : switch (type) {
            case ALL -> "Tất cả";
            case INCOME -> "Thu";
            case EXPENSE -> "Chi";
        };
        ActionLogger.logAction(String.format(
                "Báo cáo Thu-Chi | Khoảng: %s | Danh mục: %s | Phân loại: %s",
                rangeText, categoryText, typeText
        ));
    }

    private static void logLoanSelection(DateRange range, List<String> statuses) {
        String rangeText = ReportUtils.describeRange(range);
        String statusText = ReportUtils.describeSelection(statuses, "Tất cả trạng thái");
        ActionLogger.logAction(String.format(
                "Báo cáo Khoản vay | Khoảng hạn trả: %s | Trạng thái: %s",
                rangeText, statusText
        ));
    }
}
