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

            Scanner sc = new Scanner(System.in);
            int option = CheckInfor.checkOp(sc, 1, 3);

            try{
                switch(option){
                    case 1: {
                        Contact c = service.createFromConsole(sc, Contact.Stats.ChNo);
                        service.save(c);
                        System.out.println("Đã lưu hợp đồng: " + c.getName());
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case 2: {
                        Contact c = service.createFromConsole(sc, Contact.Stats.CoNo);
                        service.save(c);
                        System.out.println("Đã lưu hợp đồng: " + c.getName());
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case 3:
                        return;
                }
            }catch (IllegalArgumentException e){
                System.out.println("✖ Dữ liệu không hợp lệ: " + e.getMessage());
                System.out.println("Nhấn Enter để quay lại menu...");
                sc.nextLine();
            }catch (Exception e) {
                System.out.println("✖ Lỗi khi lưu hợp đồng: " + e.getMessage());
                System.out.println("Nhấn Enter để quay lại menu...");
                sc.nextLine();
            }
        }
    }
}
