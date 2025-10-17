package app.loan;
import java.util.Scanner;

public class MenuLoan{
    private static boolean use = true;
    private static int option;

    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        while(use) {
            System.out.println("==========Menu Loan==========");
            System.out.println("1. Tạo hợp đồng về vay/cho vay");
            System.out.println("2. Cập nhật lại hợp đồng");
            System.out.println("3. Xóa hợp đồng");
            System.out.println("4. Xem số ngày khi đến hạn");
            System.out.println("5. Xem số tiền lãi");
            System.out.println("6. Quay lại menu chính");
            option = CheckInfor.checkOp(sc,1,6);
            switch (option) {
                case 1:{
                    MakeContact.showMenu();
                    break;
                }
                case 2:{
                    UpdateContact.update(sc);
                    break;
                }
                case 3:{
                    DeleteContact.delete(sc);
                    break;
                }
                case 4:{
                    DateService.Datecheck();
                    break;
                }
                case 5:{
                    InterestService.Menu();
                    break;
                }
                case 6:{
                    System.out.println("Pái pai nha");
                    use = false;
                    break;
                }
            }
        }
    }
}
