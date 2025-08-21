package bai_1;
import java.util.Scanner;

public class InputHelper {
    public static void add_officer_new(Manage menu, Scanner sc){
        System.out.println("Loại cán bộ : 1-Công nhân, 2-Kỹ sư, 3-Nhân viên");
        int type = enter_number(sc, "Chọn loại (1-3):", 1, 3);
        String name = enter_string(sc, "Họ tên: ");
        int age = enter_number(sc, "Tuổi: ", 1, 120);
        String gender = enter_sex(sc);
        String address = enter_string(sc, "Địa chỉ: ");

        switch(type){
            case 1:
                int level = enter_number(sc, "Bậc (1-10): ",1, 10);
                menu.add_Officer(new Worker(name, age, gender, address, level));
                break;
            case 2:
                String industry = enter_string(sc, "Ngành đào tạo: ");
                menu.add_Officer(new Engineer(name, age, gender, address, industry));
                break;
            case 3: 
                String job = enter_string(sc, "Công việc: ");
                menu.add_Officer(new Staff(name, age, gender, address, job));
                break;
        }
        System.out.println(">>> Đã Thêm !!!");    
    }
    public static String enter_string(Scanner sc, String str){
        while(true){
            System.out.print(str);
            String s = sc.nextLine().trim();
            if(!s.isEmpty())    return s;
            System.out.println("Không để trống !!!");
        }
    }
    public static int enter_number(Scanner sc, String str, int min, int max){
        while(true){
            System.out.print(str);
            try{
                int x = Integer.parseInt(sc.nextLine().trim());
                if(x < min || x > max){
                    System.out.println("Chỉ nhập trong khoảng [" + min + ", " + max + "]");
                }
                else return x;    
            }catch(Exception e){
                System.out.println("Nhập số hợp lệ!");    
            }
        }
    }
    public static String enter_sex(Scanner sc){
        while(true){
            System.out.print("Nhập giới tính (Nam / Nữ / Khác): ");
            String g = sc.nextLine().trim();
            if(g.equals("Nam") || g.equals("Nữ") || g.equals("Khác") || g.equals("Nu")){
                return g.equals("Nu") ? "Nữ" : g;
            }
            System.out.println("Chỉ nhập : Nam , Nữ, Khác");
        }    
    }
}
