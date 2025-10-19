package app.ui.menu;
import app.account.*;
import java.util.Objects;
import java.util.Scanner;
import app.util.*;

public class MenuAccount {
    private final FinanceManager financeManager;
    private final Scanner scanner;
    private final AccountMenu accountMenu;
    private final TransferMenu transferMenu;
    private final ReportMenu reportMenu;

    public MenuAccount(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.accountMenu = new AccountMenu(this.financeManager, this.scanner);
        this.transferMenu = new TransferMenu(this.financeManager, this.scanner);
        this.reportMenu = new ReportMenu(this.financeManager, this.scanner);
    }

    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("QUẢN LÝ TÀI CHÍNH CÁ NHÂN");
            System.out.println("1) Thêm/xóa/chỉnh sửa tài khoản");
            System.out.println("2) Chuyển khoản giữa các tài khoản");
            System.out.println("3) Theo dõi số dư cho từng tài khoản");
            System.out.println("4) Báo cáo tài chính theo tài khoản");
            System.out.println("0) Thoát");
            System.out.print("Bạn muốn : ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        accountMenu.showMenu();
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        transferMenu.showMenu();
                        ConsoleUtils.pause(scanner);
                    }
                    case "3" -> {
                        reportMenu.showBalances();
                        ConsoleUtils.pause(scanner);
                    }
                    case "4" -> {
                        reportMenu.showReport();
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        System.out.println("Tạm biệt!");
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 4!");
                }
            } catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }
}
