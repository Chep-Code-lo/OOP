package app;

import java.util.Scanner;
import app.ui.ConsoleUtils;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("Menu Loan");

            System.out.println("1. Tạo hợp đồng");
            System.out.println("2. Cập nhật hợp đồng");
            System.out.println("3. Xóa hợp đồng");
            System.out.println("4. Hẹn nhắc nhở");
            System.out.println("5. Xem lãi");
            System.out.println("6. Thoát");

            int choice = sc.nextInt(); sc.nextLine(); // đọc bỏ \n

            switch (choice) {
                case 1 -> {
                    System.out.println("📄 Tạo hợp đồng...");
                    ConsoleUtils.pause(sc);
                }
                case 6 -> {
                    if (ConsoleUtils.confirm(sc, "Bạn có chắc chắn muốn thoát?")) {
                        System.out.println("👋 Tạm biệt!");
                        return;
                    }
                }
                default -> {
                    System.out.println("❗ Lựa chọn không hợp lệ!");
                    ConsoleUtils.pause(sc);
                }
            }
        }
    }
}
