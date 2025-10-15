package app.ui;
import app.loan.*;
import java.util.Scanner;

public class MenuLoan{
    public void showMenu(Scanner sc){
        ContactMenu menu = new ContactMenu();
        while(true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("Menu Chính");
            System.out.println("1. Tạo hợp đồng về vay/cho vay");
            System.out.println("2. Cập nhật lại hợp đồng");
            System.out.println("3. Xóa hợp đồng");
            System.out.println("4. Hẹn nhắc nhở");
            System.out.println("5. Xem lãi");
            System.out.println("6. Quay lại menu chính");
            System.out.print("Bạn muốn : ");
            int choice = sc.nextInt(); sc.nextLine();
            switch (choice) {
                case 1->{
                    menu.show();
                    sc.nextLine();
                }
                case 2 ->{
                    ConsoleUtils.clear();

                    ConsoleUtils.pause(sc);
                }
                case 3 ->{
                    ConsoleUtils.clear();

                    ConsoleUtils.pause(sc);

                }
                case 4 ->{
                    ConsoleUtils.clear();

                    ConsoleUtils.pause(sc);

                }
                case 5 ->{
                    ConsoleUtils.clear();

                    ConsoleUtils.pause(sc);

                }
                case 6 ->{
                    return;
                }
                default -> {
                    System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 1 đến 6!");
                    ConsoleUtils.pause(sc);
                }
            }
        }
    }
}
