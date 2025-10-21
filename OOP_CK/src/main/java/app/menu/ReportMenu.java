package app.menu;

import app.service.FinanceManager;
import app.util.ConsoleUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/** Menu hiển thị số dư và báo cáo thu chi. */
public class ReportMenu {
    private final FinanceManager financeManager;
    private final Scanner scanner;

    /** Khởi tạo menu báo cáo với facade tài chính và thiết bị nhập liệu dùng chung. */
    public ReportMenu(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    /** In danh sách số dư cho từng tài khoản. */
    public void showBalances() {
        ConsoleUtils.printHeader("SỐ DƯ TỪNG TÀI KHOẢN");
        if (financeManager.listAccounts().isEmpty()) {
            System.out.println("(Chưa có tài khoản nào. Vào mục 1 để tạo tài khoản trước.)");
            return;
        }
        financeManager.listAccounts().forEach(a ->
                System.out.printf("- %s | ID=%s | Số dư=%s VND%n",
                        a.getName(), a.getId(), a.getBalance().toPlainString())
        );
    }

    /** Trình đơn báo cáo thu chi theo tất cả tài khoản hoặc một tài khoản cụ thể. */
    public void showReport() {
        while (true) {
            ConsoleUtils.printHeader("BÁO CÁO THU/CHI");

            if (financeManager.listAccounts().isEmpty()) {
                System.out.println("(Chưa có tài khoản nào. Vào mục 1 để tạo tài khoản trước.)");
                ConsoleUtils.pause(scanner);
                return;
            }

            System.out.println("1) Tất cả tài khoản");
            System.out.println("2) Một tài khoản cụ thể");
            System.out.println("0) Quay lại");
            System.out.print("Chọn: ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        Map<String, BigDecimal> report = financeManager.getReportService().summaryAll();
                        printSummary(report);
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        financeManager.listAccounts().forEach(a ->
                                System.out.printf("- %s | ID=%s%n", a.getName(), a.getId())
                        );
                        System.out.print("Nhập ID tài khoản: ");
                        String id = scanner.nextLine().trim();

                        boolean exists = financeManager.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
                        if (!exists) {
                            System.out.println("Không tìm thấy tài khoản: " + id);
                            ConsoleUtils.pause(scanner);
                            break;
                        }
                        Map<String, BigDecimal> report = financeManager.getReportService()
                                .summaryForAccount(id);
                        printSummary(report);
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        return;
                    }
                    default -> {
                        System.out.println("Chỉ nhận 0–2.");
                        ConsoleUtils.pause(scanner);
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi : " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }

    /** In số liệu tổng hợp thu/chi/chênh lệch ra màn hình. */
    private void printSummary(Map<String, BigDecimal> report) {
        Map<String, BigDecimal> rpt = (report == null) ? Collections.emptyMap() : report;
        BigDecimal income = rpt.getOrDefault("income", BigDecimal.ZERO);
        BigDecimal expense = rpt.getOrDefault("expense", BigDecimal.ZERO);
        BigDecimal net = rpt.getOrDefault("net", BigDecimal.ZERO);

        System.out.println("\n--- KẾT QUẢ ---");
        System.out.println("Tổng THU   : " + income.toPlainString() + " VND");
        System.out.println("Tổng CHI   : " + expense.toPlainString() + " VND");
        System.out.println("Chênh lệch : " + net.toPlainString() + " VND");
    }
}
