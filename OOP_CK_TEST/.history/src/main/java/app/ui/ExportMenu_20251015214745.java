package app.ui;
import app.loan.*;
import app.export.*;
import java.util.Scanner;

public class ExportMenu {
    public void show(Scanner sc) {
        while(true){
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("LƯU DỮ LIỆU CSV");
            System.out.println("1. Lưu Tài khoản");
            System.out.println("2. Lưu Giao dịch");
            System.out.println("3. Lưu Khoản vay");
            System.out.println("4. Lưu Lịch sử trả");
            System.out.println("5. Lưu tất cả");
            System.out.println("0. Quay lại");
            System.out.print("Bạn muốn : ");

            try{
                switch (sc.nextLine().trim()) {
                    case "1" -> {
                        save(() -> ExportAccounts.export(), "accounts.csv");
                        ConsoleUtils.pause(sc);
                    }
                    case "2" -> {
                        save(() -> ExportTransactions.export(), "transactions.csv");
                        ConsoleUtils.pause(sc);
                    }
                    case "3" -> {
                        save(() -> ExportLoans.export(), "loans.csv");
                        ConsoleUtils.pause(sc);
                    }
                    case "4" -> {
                        save(() -> ExportLoanPayments.export(), "loan_payments.csv");
                        ConsoleUtils.pause(sc);
                    }
                    case "5" -> {
                        save(() -> ExportAccounts.export(), "accounts.csv");
                        save(() -> ExportTransactions.export(), "transactions.csv");
                        save(() -> ExportLoans.export(), "loans.csv");
                        save(() -> ExportLoanPayments.export(), "loan_payments.csv");
                        ConsoleUtils.pause(sc);
                    }
                    case "0" -> {
                        return; 
                    }
                    default -> {
                        System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 1 đến 6!");
                        ConsoleUtils.pause(sc);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(sc);
            }
        }
    }

    private static void save(Saver saver, String name){
        try {
            int flushed = ContractStorage.flushPending();
            if (flushed > 0) {
                System.out.println("Đã chuyển " + flushed + " hợp đồng chờ vào dữ liệu sẵn sàng để xuất.");
            }
            var path = saver.run();
            System.out.println("Đã lưu " + name + " → " + path.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Lỗi lưu " + name + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface Saver { java.nio.file.Path run() throws Exception; }
}
