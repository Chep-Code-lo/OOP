package app.account;

import app.ui.ConsoleUtils;
import java.util.Objects;
import java.util.Scanner;

/** Menu chuyển khoản giữa các tài khoản. */
public class TransferMenu {
    private final FinanceManager financeManager;
    private final AccountActions actions;
    private final Scanner scanner;

    public TransferMenu(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.actions = new AccountActions(this.financeManager, this.scanner);
    }

    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("CHUYỂN KHOẢN");

            int count = financeManager.listAccounts().size();
            if (count < 2) {
                if (count == 0) {
                    System.out.println("(Chưa có tài khoản nào. Vào menu 1 để tạo tài khoản trước.)");
                } else {
                    System.out.println("(Chỉ có 1 tài khoản. Cần ít nhất 2 tài khoản để chuyển khoản.)");
                }
                System.out.print("\nNhấn Enter để quay lại...");
                scanner.nextLine();
                return;
            }

            System.out.println("1) Thực hiện chuyển khoản");
            System.out.println("2) Quay lại menu chính");
            System.out.print("Bạn muốn: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> actions.transferBetweenAccounts();
                    case "2" -> {
                        return;
                    }
                    default -> {
                        System.out.println("Lựa chọn không hợp lệ. Nhấn Enter để thử lại...");
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
