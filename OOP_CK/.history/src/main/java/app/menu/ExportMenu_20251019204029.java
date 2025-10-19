package app.menu;

import app.export.ExportAccounts;
import app.export.ExportLoanPayments;
import app.export.ExportLoans;
import app.export.ExportTransactions;
import app.loan.ContractStorage;
import java.util.Objects;
import java.util.Scanner;

public class ExportMenu {
    private final Scanner scanner;

    public ExportMenu(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public void show() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("LƯU DỮ LIỆU CSV");
            System.out.println("1. Lưu Tài khoản");
            System.out.println("2. Lưu Giao dịch");
            System.out.println("3. Lưu Khoản vay");
            System.out.println("4. Lưu Lịch sử trả");
            System.out.println("5. Lưu tất cả");
            System.out.println("0. Quay lại");
            System.out.print("Bạn muốn : ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        save(() -> ExportAccounts.export(), "accounts.csv");
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        save(() -> ExportTransactions.export(), "transactions.csv");
                        ConsoleUtils.pause(scanner);
                    }
                    case "3" -> {
                        save(() -> ExportLoans.export(), "loans.csv");
                        ConsoleUtils.pause(scanner);
                    }
                    case "4" -> {
                        save(() -> ExportLoanPayments.export(), "loan_payments.csv");
                        ConsoleUtils.pause(scanner);
                    }
                    case "5" -> {
                        save(() -> ExportAccounts.export(), "accounts.csv");
                        save(() -> ExportTransactions.export(), "transactions.csv");
                        save(() -> ExportLoans.export(), "loans.csv");
                        save(() -> ExportLoanPayments.export(), "loan_payments.csv");
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        return;
                    }
                    default -> {
                        System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 5!");
                        ConsoleUtils.pause(scanner);
                    }
                }
            } catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }

    private void save(Saver saver, String name) {
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
