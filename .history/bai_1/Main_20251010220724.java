package bai_1;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args){
        Manage menu = new Manage();

        // ✅ In menu 1 lần lúc khởi động
        printMenuOnce();

        while (true) {
            System.out.print("\n👉 Nhập lựa chọn (0-3): ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    InputHelper.add_officer_new(menu, sc);
                    System.out.println("✅ Đã thêm cán bộ mới thành công!");
                    pauseEnter();
                    clearScreenAndShowMenu();   // ⬅️ clear + hiện lại menu ở trên
                    break;

                case "2":
                    System.out.print("🔎 Nhập tên (hoặc từ khóa) cần tìm: ");
                    String kw = sc.nextLine().trim();
                    List<Officer> ans = menu.Search_name(kw);
                    if (ans == null || ans.isEmpty()) {
                        System.out.println("❌ Không tìm thấy!");
                    } else {
                        ans.forEach(System.out::println);
                    }
                    pauseEnter();
                    clearScreenAndShowMenu();
                    break;

                case "3":
                    List<Officer> all = menu.getAll();
                    if (all == null || all.isEmpty()) {
                        System.out.println("📭 Danh sách rỗng!");
                    } else {
                        all.forEach(System.out::println);
                    }
                    pauseEnter();
                    clearScreenAndShowMenu();
                    break;

                case "0":
                    System.out.println("👋 Tạm biệt!");
                    return;

                default:
                    System.out.println("⚠️ Lựa chọn không hợp lệ, chỉ nhập 0-3!");
                    pauseEnter();
                    clearScreenAndShowMenu();
            }
        }
    }

    // ===== Helpers =====
    private static void printMenuOnce() {
        System.out.println("===== QUẢN LÝ CÁN BỘ =====");
        System.out.println("1. Thêm mới cán bộ");
        System.out.println("2. Tìm kiếm theo họ tên");
        System.out.println("3. Hiển thị thông tin về danh sách các cán bộ");
        System.out.println("0. Thoát khỏi chương trình");
    }

    private static void pauseEnter() {
        System.out.print("\n⏸ Nhấn Enter để tiếp tục...");
        sc.nextLine();
    }

    /** Xóa màn hình và IN LẠI MENU ngay sau đó để menu luôn ở đầu. */
    private static void clearScreenAndShowMenu() {
        // Thử ANSI (đa số terminal/Windows 10+ hỗ trợ)
        try {
            System.out.print("\033[H\033[2J"); // cursor home + clear screen
            System.out.flush();
        } catch (Exception ignored) {}

        // Dự phòng: gọi lệnh hệ điều hành
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb = os.contains("win")
                    ? new ProcessBuilder("cmd", "/c", "cls")
                    : new ProcessBuilder("clear");
            pb.inheritIO().start().waitFor();
        } catch (Exception ignored) {}

        // ✅ In lại menu để giữ cố định ở đầu
        printMenuOnce();
    }
}
