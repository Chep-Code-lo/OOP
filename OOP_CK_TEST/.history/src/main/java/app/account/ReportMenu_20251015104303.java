package app.account;

import org.example.finance.app.FinanceManager;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Scanner;

public class ReportMenu {

    /** Hiển thị số dư từng tài khoản  */
    public void showBalances(Scanner sc, FinanceManager fm) {
        ConsoleUtils.printHeader("SỐ DƯ TỪNG TÀI KHOẢN"); // tự clear trước khi in
        if (fm.listAccounts().isEmpty()) {
            System.out.println("(Chưa có tài khoản nào. Vào mục 1 để tạo tài khoản trước.)");
            return;
        }
        fm.listAccounts().forEach(a ->
                System.out.printf("• %s | ID=%s | Số dư=%s VND%n",
                        a.getName(), a.getId(), a.getBalance().toPlainString())
        );
    }

    /** Menu báo cáo thu/chi: TẤT CẢ hoặc 1 TÀI KHOẢN. */
    public void showReport(Scanner sc, FinanceManager fm) {
        while (true) {
            ConsoleUtils.printHeader("BÁO CÁO THU/CHI");

            //chưa có tài khoản -> báo và quay lại
            if (fm.listAccounts().isEmpty()) {
                System.out.println("(Chưa có tài khoản nào. Vào mục 1 để tạo tài khoản trước.)");
                pause(sc);
                return; // QUAN TRỌNG
            }

            System.out.println("1) Tất cả tài khoản");
            System.out.println("2) Một tài khoản cụ thể");
            System.out.println("0) Quay lại");
            System.out.print("Chọn: ");

            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": {
                        // TỔNG HỢP TẤT CẢ TÀI KHOẢN
                        Map<String, BigDecimal> rpt = fm.getReportService().summaryAll();
                        printSummary(rpt);
                        pause(sc);
                        break;
                    }
                    case "2": {
                        // Liệt kê & chọn tài khoản
                        fm.listAccounts().forEach(a ->
                                System.out.printf("• %s | ID=%s%n", a.getName(), a.getId())
                        );
                        System.out.print("Nhập ID tài khoản: ");
                        String id = sc.nextLine().trim();

                        boolean exists = fm.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
                        if (!exists) {
                            System.out.println("Không tìm thấy tài khoản: " + id);
                            pause(sc);
                            break;
                        }

                        Map<String, BigDecimal> rpt = fm.getReportService().summaryForAccount(id);
                        printSummary(rpt);
                        pause(sc);
                        break;
                    }
                    case "0":
                        return;
                    default:
                        System.out.println("Chỉ nhận 0–2.");
                        pause(sc);
                }
            } catch (Exception e) {
                System.out.println("Lỗi khi tạo báo cáo: " + e.getMessage());
                pause(sc);
            }
        }
    }

    // ===== Helpers =====

    private void printSummary(Map<String, BigDecimal> rpt) {
        BigDecimal income  = rpt.getOrDefault("income",  BigDecimal.ZERO);
        BigDecimal expense = rpt.getOrDefault("expense", BigDecimal.ZERO);
        BigDecimal net     = rpt.getOrDefault("net",     BigDecimal.ZERO);

        System.out.println("\n--- KẾT QUẢ ---");
        System.out.println("Tổng THU   : " + income.toPlainString()  + " VND");
        System.out.println("Tổng CHI   : " + expense.toPlainString() + " VND");
        System.out.println("Chênh lệch : " + net.toPlainString()     + " VND");
    }

    private void pause(Scanner sc) {
        System.out.print("\nNhấn Enter để quay lại...");
        sc.nextLine();
    }
}
