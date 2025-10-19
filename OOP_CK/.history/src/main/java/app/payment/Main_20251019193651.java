package app.payment;

import app.store.DataStore;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        start();
    }

    /** Cho module khác (menu chính app.App) gọi mở menu báo cáo */
    public static void start() {
        while (true) {
            clearConsole();
            printMenu();
            String choice = SC.nextLine().trim();
            switch (choice) {
                case "1" -> runIncomeExpenseWithFilters(); // Thu - Chi: lọc Ngày + Danh mục + Phân loại
                case "2" -> runLoanWithFilters();          // Khoản vay: lọc Ngày (hạn trả) + Trạng thái
                case "3" -> AccountReport.run();           // Tài khoản
                case "0" -> {
                    System.out.println("Tạm biệt!");
                    return; // thoát menu báo cáo
                }
                default -> {
                    System.out.println("Lựa chọn không hợp lệ. Nhấn Enter...");
                    SC.nextLine();
                }
            }
        }
    }

    // ===== Thu - Chi: nhập bộ lọc rồi chạy
    private static void runIncomeExpenseWithFilters() {
        clearConsole();
        System.out.println("===== BÁO CÁO THU - CHI (có lọc) =====");

        DateRange range = inputDateRange();
        List<String> categories = inputCategories(); // có thể rỗng
        IncomeExpenseReport.TxClass type = inputTxClass(); // Tất cả / Thu / Chi

        IncomeExpenseReport.run(range, categories, type);

        pause();
    }

    // ===== Khoản vay: lọc theo hạn trả + Trạng thái
    private static void runLoanWithFilters() {
        clearConsole();
        System.out.println("===== BÁO CÁO KHOẢN VAY (lọc hạn trả + trạng thái) =====");

        DateRange range = inputDateRange();
        List<String> statuses = inputLoanStatuses(); // có thể rỗng

        LoanReport.run(range, statuses);

        pause();
    }

    // === Helpers nhập liệu chung ===
    private static DateRange inputDateRange() {
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

    private static LocalDate readDateOrNull(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (s.isEmpty()) return null;
            try {
                return LocalDate.parse(s); // yyyy-MM-dd
            } catch (DateTimeParseException e) {
                System.out.println("Không đúng định dạng (yyyy-MM-dd). Nhập lại hoặc Enter để bỏ qua.");
            }
        }
    }

    /** Lấy danh mục từ dữ liệu hiện có và cho phép chọn nhiều */
    private static List<String> inputCategories() {
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
        String raw = SC.nextLine().trim();
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
    private static IncomeExpenseReport.TxClass inputTxClass() {
        while (true) {
            System.out.println("Chọn phân loại:");
            System.out.println("1) Tất cả");
            System.out.println("2) Chỉ THU");
            System.out.println("3) Chỉ CHI");
            System.out.print("Chọn: ");
            String s = SC.nextLine().trim();
            switch (s) {
                case "1", "" -> { return IncomeExpenseReport.TxClass.ALL; }
                case "2" -> { return IncomeExpenseReport.TxClass.INCOME; }
                case "3" -> { return IncomeExpenseReport.TxClass.EXPENSE; }
                default -> System.out.println("Không hợp lệ, nhập lại.");
            }
        }
    }

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
        String raw = SC.nextLine().trim();
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

    private static void pause() {
        System.out.print("Nhấn Enter để tiếp tục...");
        SC.nextLine();
    }

    private static void clearConsole() {
        for (int i = 0; i < 40; i++) System.out.println();
    }

    private static void printMenu() {
        System.out.println("========== MENU BÁO CÁO ==========");
        System.out.println("1) Báo cáo Thu - Chi (lọc theo ngày, danh mục, phân loại)");
        System.out.println("2) Báo cáo Khoản Vay (lọc theo hạn trả, trạng thái)");
        System.out.println("3) Báo cáo Tài Khoản");
        System.out.println("0) Thoát");
        System.out.print("Chọn: ");
    }
}
