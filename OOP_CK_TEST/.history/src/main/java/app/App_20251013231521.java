package app;
import app.loan.*;
import app.export.*;
import java.util.Scanner;
import app.ui.ConsoleUtils;
public class App{
    public static void main(String[] args){
    Scanner sc = new Scanner(System.in);
    MenuLoan loanMenu = new MenuLoan();
        while(true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("Menu Chính");
            System.out.println("1. Quản lý tài khoản tài chính");
            System.out.println("2. Quản lý giao dịch thu chi");
            System.out.println("3. Quản lý khoản vay và cho vay");
            System.out.println("4. Báo cáo và thống kê tài chính");
            System.out.println("5. Xuất file");
            System.out.println("6. Thoát");
            System.out.print("Bạn muốn : ");
            int choice = sc.nextInt();sc.nextLine();
            switch (choice) {
                case 1->{
                    
                    sc.nextLine();
                }
                case 2 ->{
                    ConsoleUtils.clear();

                    ConsoleUtils.pause(sc);
                }
                case 3 ->{
                    loanMenu.showMenu(sc);
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
                    System.out.println("Pái pai nha");
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

