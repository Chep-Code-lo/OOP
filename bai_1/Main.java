package bai_1;
import java.util.List;
import java.util.Scanner;
public class Main {
    private static final Scanner sc = new Scanner(System.in);
    public static void main(String[] args){
        Manage menu = new Manage();
        while(true){
            System.out.println("\n===== QUẢN LÝ CÁN BỘ =====");
            System.out.println("1.Thêm mới cán bộ");
            System.out.println("2.Tìm kiếm theo họ tên");
            System.out.println("3.Hiển thị thông tin về danh sách các cán bộ");
            System.out.println("0.Thoát khỏi chương trình");
            System.out.println("Chọn: ");
            String chosse = sc.nextLine().trim();

            switch(chosse){
                case "1":
                    add_officer_new(menu);
                    break;
                case "2":
                    System.out.println("Nhập tên (hoặc từ khóa) cần tìm : ");
                    String kw = sc.nextLine().trim();
                    List<Officer> ans = menu.Search_name(kw);
                    if(kw.isEmpty())    System.out.println("Không tìm thấy!");
                    else ans.forEach(System.out::println);
                    break;
                case "3":
                    List<Officer> all = menu.getAll();
                    if(all.isEmpty())   System.out.println("Danh sách rỗng!");
                    else all.forEach(System.out::println);
                    break;
                case "0":
                    System.out.println("Tạm biệt!");
                    return;
                default:
                    System.out.println("Chỉ chọn 0-3 thôi nhé!!!");
            }
        }
    }
    private static void add_officer_new(Manage menu){
        System.out.println("Loại cán bộ : 1-Công nhân, 2-Kỹ sư, 3-Nhân viên");
        int type = enter_number("Chọn loại (1-3):", 1, 3);
        String name = enter_string("Họ tên: ");
        int age = enter_number("Tuổi: ", 1, 120);
        String gender = enter_sex();
        String address = enter_string("Địa chỉ: ");
        switch (type) {
            case 1:
                int level = enter_number("Bậc (1-10): ",1, 10);
                menu.add_Officer(new Worker(name, age, gender, address, level));
                break;
            case 2:
                String industry = enter_string("Ngành đào tạo: ");
                menu.add_Officer(new Engineer(name, age, gender, address, industry));
                break;
            case 3: 
                String job = enter_string("Công việc: ");
                menu.add_Officer(new Staff(name, age, gender, address, job));
            default:
                break;
        }
        System.out.println(">>> Đã Thêm !!!");    
    }
    private static String enter_string(String str){
        while(true){
            System.out.println(str);
            String s = sc.nextLine().trim();
            if(!s.isEmpty())    return s;
            System.out.println("Không để trống !!!");
        }
    }
    private static int enter_number(String str, int min, int max){
        while(true){
            System.out.println(str);
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
    private static String enter_sex(){
        while(true){
            System.out.println("Nhập giới tính (Nam / Nữ / Khác): ");
            String g = sc.nextLine().trim();
            if(g.equals("Nam") || g.equals("Nữ") || g.equals("Khác") || g.equals("Nu")){
                return g.equals("Nu") ? "Nữ " : g;
            }
            System.out.println("Chỉ nhập : Nam , Nữ, Khác");
        }    
    }
}