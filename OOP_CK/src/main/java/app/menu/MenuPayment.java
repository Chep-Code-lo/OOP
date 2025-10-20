package app.menu;

import app.model.DateRange;
import app.report.AccountReport;
import app.report.IncomeExpenseReport;
import app.report.LoanReport;
import app.util.ConsoleUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Menu tổng hợp cho các báo cáo tài chính (thu-chi, vay, tài khoản). */
public class MenuPayment {
    private final Scanner scanner = new Scanner(System.in);

    /** Vòng lặp hiển thị menu báo cáo và kích hoạt báo cáo tương ứng. */
    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("MENU BÁO CÁO");
            System.out.println("1) Báo cáo Thu - Chi (lọc theo ngày, danh mục, phân loại)");
            System.out.println("2) Báo cáo Khoản Vay (lọc theo hạn trả, trạng thái)");
            System.out.println("3) Báo cáo Tài Khoản");
            System.out.println("0) Thoát");
            System.out.print("Chọn: ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        runIncomeExpenseReport();
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        runLoanReport();
                        ConsoleUtils.pause(scanner);
                    }
                    case "3" -> {
                        AccountReport.run();
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        System.out.println("Tạm biệt!");
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 3!");
                }
            } catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }

    /** Thu thập tham số lọc và chạy báo cáo Thu - Chi. */
    private void runIncomeExpenseReport() {
        ConsoleUtils.printHeader("BÁO CÁO THU - CHI");
        DateRange range = readDateRange();
        List<String> categories = readList("Danh mục (nhập nhiều, cách nhau bằng dấu phẩy, bỏ trống = tất cả): ");
        IncomeExpenseReport.TxClass txClass = MenuIncomeExpense.inputTxClass();
        IncomeExpenseReport.run(range, categories, txClass);
    }

    /** Thu thập tham số lọc và chạy báo cáo Khoản vay. */
    private void runLoanReport() {
        ConsoleUtils.printHeader("BÁO CÁO KHOẢN VAY");
        DateRange range = readDateRange();
        List<String> statuses = readList("Trạng thái muốn lọc (ví dụ: dang no, da tra) - bỏ trống = tất cả: ");
        LoanReport.run(range, statuses);
    }

    /** Đọc khoảng ngày báo cáo, tự động mở rộng nếu người dùng bỏ trống. */
    private DateRange readDateRange() {
        LocalDate start = readDate("Từ ngày (YYYY-MM-DD, Enter để bỏ qua): ");
        LocalDate end = readDate("Đến ngày (YYYY-MM-DD, Enter để bỏ qua): ");
        if (start == null && end == null) {
            return DateRange.allTime();
        }
        LocalDate effectiveStart = (start == null) ? LocalDate.MIN : start;
        LocalDate effectiveEnd = (end == null) ? LocalDate.MAX : end;
        if (effectiveEnd.isBefore(effectiveStart)) {
            System.out.println("Ngày kết thúc phải sau ngày bắt đầu. Sử dụng toàn bộ khoảng thời gian.");
            return DateRange.allTime();
        }
        return new DateRange(effectiveStart, effectiveEnd);
    }

    /** Đọc một ngày hợp lệ theo định dạng YYYY-MM-DD, trả về null nếu bỏ trống. */
    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("Ngày không hợp lệ, định dạng phải là YYYY-MM-DD.");
            }
        }
    }

    /** Đọc danh sách giá trị dạng CSV đơn giản từ người dùng. */
    private List<String> readList(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return List.of();
        }
        String[] parts = input.split(",");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
