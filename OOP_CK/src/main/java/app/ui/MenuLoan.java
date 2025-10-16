package app.ui;

import app.loan.ContactMenu;
import java.util.Objects;
import java.util.Scanner;

public class MenuLoan {
    private final ContactMenu contactMenu;
    private final Scanner scanner;

    public MenuLoan(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.contactMenu = new ContactMenu();
    }

    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("Menu Chính");
            System.out.println("1. Tạo hợp đồng về vay/cho vay");
            System.out.println("2. Cập nhật lại hợp đồng");
            System.out.println("3. Xóa hợp đồng");
            System.out.println("4. Hẹn nhắc nhở");
            System.out.println("5. Xem lãi");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Bạn muốn : ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> contactMenu.show(scanner);
                    case "2", "3", "4", "5" -> {
                        ConsoleUtils.clear();
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        return;
                    }
                    default -> {
                        System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 1 đến 6!");
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
