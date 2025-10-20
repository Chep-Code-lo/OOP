package app.menu;

import app.service.AccountActions;
import app.service.FinanceManager;
import app.util.ConsoleUtils;
import java.util.Objects;
import java.util.Scanner;

/** Menu quản lý tài khoản, sử dụng AccountActions để thao tác nghiệp vụ. */
public class AccountMenu {
    private final FinanceManager financeManager;
    private final AccountActions actions;
    private final Scanner scanner;

    /** Khởi tạo menu với lớp nghiệp vụ và nguồn nhập liệu dùng chung. */
    public AccountMenu(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.actions = new AccountActions(this.financeManager, this.scanner);
    }

    /** Vòng lặp hiển thị menu quản lý tài khoản và điều hướng hành động người dùng. */
    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("TÀI KHOẢN");
            System.out.println("1) Thêm tài khoản");
            System.out.println("2) Đổi tên tài khoản");
            System.out.println("3) Xóa tài khoản (chỉ khi số dư = 0)");
            System.out.println("4) Hiển thị danh sách tài khoản");
            System.out.println("0) Quay lại");
            System.out.print("Chọn (0-4): ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        actions.addAccount();
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        actions.renameAccount();
                        ConsoleUtils.pause(scanner);
                    }
                    case "3" -> {
                        actions.deleteAccount();
                        ConsoleUtils.pause(scanner);
                    }
                    case "4" -> {
                        ConsoleUtils.clear();
                        ConsoleUtils.printHeader("DANH SÁCH TÀI KHOẢN");
                        actions.listAccounts();
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        return;
                    }
                    default -> {
                        System.out.println("Giá trị không hợp lệ. Vui lòng nhập số từ 0 đến 4.");
                        ConsoleUtils.pause(scanner);
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi: " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }
}
