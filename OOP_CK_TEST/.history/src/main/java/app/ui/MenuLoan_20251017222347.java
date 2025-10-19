package app.ui;

import app.loan.*;
import java.util.Scanner;

public class MenuLoan {
    Scanner sc = new Scanner(System.in);
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
                switch (sc.nextLine().trim()) {
                    case "1" -> {
                        MakeContact.showMenu();
                        ConsoleUtils.pause(sc);
                    }
                    case "2" -> {
                        UpdateContact.update(sc);
                        ConsoleUtils.pause(sc);
                    }
                    case "3" -> {
                        DeleteContact.delete(sc);
                        ConsoleUtils.pause(sc);
                    }
                    case "4" -> {
                        DateService.Datecheck();
                        ConsoleUtils.pause(sc);
                    }
                    case "5" ->{
                        InterestService.Menu();
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
                ConsoleUtils.pause(sc);
            }
        }
    }
}
