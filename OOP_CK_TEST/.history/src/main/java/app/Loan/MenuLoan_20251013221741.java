import app.ui.*;
import java.util.Scanner;

public class MenuLoan{
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
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
            int choice = CheckInfor.checkOp(sc, 1, 6);
            switch (choice) {
                case 1->{
                    menu.show();
                    System.out.println("\nNhấn Enter để quay lại menu Loan...");
                    sc.nextLine();
                }
                case 2 ->{
                    ConsoleUtils.clear();
                    
                    pause(sc);
                }
                case 3 ->{
                    ConsoleUtils.clear();
                    
                    pause(sc);
                    
                }
                case 4 ->{
                    ConsoleUtils.clear();
                    
                    pause(sc);
                    
                }
                case 5 ->{
                    ConsoleUtils.clear();
                    
                    pause(sc);
                    
                }
                case 6 ->{
                    System.out.println("Pái pai nha");
                    return;
                }
                default -> {
                    // (CheckInfor.checkOp đã kiểm tra rồi, nên nhánh này gần như không xảy ra)
                    System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 1 đến 6!");
                    pause(sc);
                }
            }
        }
    }
    private static void pause(Scanner sc) {
        System.out.println("\nNhấn Enter để quay lại menu...");
        sc.nextLine();
    }
}
