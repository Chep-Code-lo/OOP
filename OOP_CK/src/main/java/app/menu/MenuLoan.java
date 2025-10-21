package app.menu;

import app.loan.DeleteContact;
import app.loan.UpdateContact;
import app.service.DateService;
import app.service.InterestService;
import app.service.FinanceManager;
import app.util.ConsoleUtils;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

/** Menu điều hướng cho các tác vụ vay/cho vay và lãi suất. */
public class MenuLoan {
    private final Scanner sc;
    private final FinanceManager financeManager;

    public MenuLoan(FinanceManager financeManager, Scanner sc) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.sc = Objects.requireNonNull(sc, "scanner");
    }

    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("MENU VAY");
            System.out.println("1. Tạo hợp đồng về vay/cho vay");
            System.out.println("2. Cập nhật lại hợp đồng");
            System.out.println("3. Xóa hợp đồng");
            System.out.println("4. Hẹn nhắc nhở");
            System.out.println("5. Xem lãi");
            System.out.println("0. Quay lại menu chính");
            System.out.print("Bạn muốn : ");

            try {
                if (!sc.hasNextLine()) {
                    return;
                }
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> {
                        MenuContact.showMenu(financeManager, sc);
                    }
                    case "2" -> {
                        UpdateContact.update(financeManager, sc);
                        ConsoleUtils.pause(sc);
                    }
                    case "3" -> {
                        DeleteContact.delete(sc);
                        ConsoleUtils.pause(sc);
                    }
                    case "4" -> {
                        DateService.Datecheck(sc);
                        ConsoleUtils.pause(sc);
                    }
                    case "5" -> {
                        InterestService.showMenu(sc);
                        ConsoleUtils.pause(sc);
                    }
                    case "0" -> {
                        System.out.println("Tạm biệt!");
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 5!");
                }
            } catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                try {
                    ConsoleUtils.pause(sc);
                } catch (NoSuchElementException ignored) {
                    // input stream exhausted (e.g., automated test); skip pause
                }
            }
        }
    }
}
