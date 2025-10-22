package app.menu;

import app.model.DateRange;
import app.report.AccountReport;
import app.report.IncomeExpenseReport;
import app.report.LoanReport;
import app.util.ConsoleUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MenuPayment {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final Scanner scanner = new Scanner(System.in);

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


    private void runIncomeExpenseReport() {
        ConsoleUtils.printHeader("BÁO CÁO THU - CHI");
        DateRange range = readDateRange();
        List<String> categories = app.report.IncomeExpenseReport.promptCategories();
        IncomeExpenseReport.TxClass txClass = MenuIncomeExpense.inputTxClass();
        IncomeExpenseReport.run(range, categories, txClass);
    }


    private void runLoanReport() {
        ConsoleUtils.printHeader("BÁO CÁO KHOẢN VAY");
        DateRange range = readDateRange();
        List<String> statuses = readList("Trạng thái muốn lọc (ví dụ: dang no, da tra) - bỏ trống = tất cả: ");
        LoanReport.run(range, statuses);
    }


    private DateRange readDateRange() {
        LocalDate start = readDate("Từ ngày (dd-MM-yyyy, Enter để bỏ qua): ");
        LocalDate end = readDate("Đến ngày (dd-MM-yyyy, Enter để bỏ qua): ");
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


    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Ngày không hợp lệ, định dạng phải là dd-MM-yyyy.");
            }
        }
    }


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
