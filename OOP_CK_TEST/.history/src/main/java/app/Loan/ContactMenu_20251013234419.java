package app.loan;
import app.ui.ConsoleUtils;
import java.util.Scanner;

public class ContactMenu{
    private final ContactService service = new ContactService();
    public void show(){
        while(true){
            ConsoleUtils.clear();//clear màn hình menu

            System.out.println("========== Menu Contact ==========");
            System.out.println("1. Cho chủ nợ ");
            System.out.println("2. Cho con nợ ");
            System.out.println("3. Quay lại menu");
            System.out.print("Chọn : ");
            Scanner sc = new Scanner(System.in);
            int option = sc.nextInt(); sc.nextLine();
            try{
                switch(option){
                    case 1: {
                        Contact c = service.createFromConsole(sc, Contact.Stats.ChNo);
                        service.queueForSave(c);
                        sc.nextLine();
                        break;
                    }
                    case 2: {
                        Contact c = service.createFromConsole(sc, Contact.Stats.CoNo);
                        service.queueForSave(c);
                        sc.nextLine();
                        break;
                    }
                    case 3:
                        return;
                    default :{
                        System.out.println("Vui lòng nhập trong khoảng từ 1 đến 3");
                    }
                }
            }catch (IllegalArgumentException e){
                System.out.println("✖ Dữ liệu không hợp lệ: " + e.getMessage());
                sc.nextLine();
            }
        }
    }
}
