package bai_1;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    public static void main(String[] args){
        Manage menu = new Manage();
        System.out.println("\n===== QUẢN LÝ CÁN BỘ =====");
        System.out.println("1.Thêm mới cán bộ");
        System.out.println("2.Tìm kiếm theo họ tên");
        System.out.println("3.Hiển thị thông tin về danh sách các cán bộ");
        System.out.println("0.Thoát khỏi chương trình");
        System.out.println("Chọn: ");
        while(true){
            String chosse = sc.nextLine().trim();
            switch(chosse){
                case "1":
                    InputHelper.add_officer_new(menu, sc);
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
}