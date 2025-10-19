package app.menu;

import app.account.FinanceManager;
import app.transaction.Action;
import app.transaction.TransactionService;
import java.util.Objects;
import java.util.Scanner;
import app.util.*;

public class Menutransaction {
    private final Action app;
    private final Scanner scanner;

    public Menutransaction(FinanceManager financeManager, Scanner scanner) {
        TransactionService service = new TransactionService(Objects.requireNonNull(financeManager, "financeManager"));
        this.app = new Action(service);
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("QUẢN LÝ GIAO DỊCH");
            System.out.println("1) Xem số dư");
            System.out.println("2) Thêm giao dịch");
            System.out.println("3) Xem lịch sử giao dịch");
            System.out.println("4) Sửa giao dịch");
            System.out.println("5) Xóa giao dịch");
            System.out.println("0) Thoát");
            System.out.print("Bạn muốn :  ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        app.showBalances();
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        app.addTxCLI(scanner);
                        ConsoleUtils.pause(scanner);
                    }
                    case "3" -> {
                        app.listTxCLI(scanner);
                        ConsoleUtils.pause(scanner);
                    }
                    case "4" -> {
                        app.editTxCLI(scanner);
                        ConsoleUtils.pause(scanner);
                    }
                    case "5" -> {
                        app.deleteTxCLI(scanner);
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        System.out.println("Bye!");
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
}
