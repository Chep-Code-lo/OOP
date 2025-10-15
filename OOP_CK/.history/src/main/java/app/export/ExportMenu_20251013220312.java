package app.export;
import app.export.ExportAccounts;
import app.export.ExportTransactions;
import app.export.ExportLoans;
import app.export.ExportLoanPayments;
import java.util.Scanner;

public class ExportMenu {
    public static void show(Scanner sc) {
        while(true){
            System.out.println("===== LƯU DỮ LIỆU CSV =====");
            System.out.println("1. Lưu Tài khoản");
            System.out.println("2. Lưu Giao dịch");
            System.out.println("3. Lưu Khoản vay");
            System.out.println("4. Lưu Lịch sử trả");
            System.out.println("5. Lưu tất cả");
            System.out.println("6. Quay lại");
            System.out.print("Chọn: ");
            switch (sc.nextLine().trim()) {
                case "1" -> save(() -> ExportAccounts.export(), "accounts.csv");
                case "2" -> save(() -> ExportTransactions.export(), "transactions.csv");
                case "3" -> save(() -> ExportLoans.export(), "loans.csv");
                case "4" -> save(() -> ExportLoanPayments.export(), "loan_payments.csv");
                case "5" -> {
                    save(() -> ExportAccounts.export(), "accounts.csv");
                    save(() -> ExportTransactions.export(), "transactions.csv");
                    save(() -> ExportLoans.export(), "loans.csv");
                    save(() -> ExportLoanPayments.export(), "loan_payments.csv");
                }
                case "6" -> { return; }
                default -> System.out.println("❌ Chọn 1-6");
            }
        }
    }

    private static void save(Saver saver, String name) {
        try {
            var path = saver.run();
            System.out.println("✅ Đã lưu " + name + " → " + path.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("❌ Lỗi lưu " + name + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface Saver { java.nio.file.Path run() throws Exception; }
}
