package app.util;
import java.util.Scanner;
/*
 * Lớp tiện ích ConsoleUtils cung cấp nhiều hàm hỗ trợ thao tác giao diện console
 * như: xóa màn hình, tạm dừng, in tiêu đề, tạo đường gạch ngang, chờ xác nhận,...
 */
public final class ConsoleUtils{
    // Không cho khởi tạo
    private ConsoleUtils() {}
    /*Xóa toàn bộ màn hình console */
    public static void clear(){
        try {
            if(System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }else{
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        }catch (Exception e){
            // fallback: in nhiều dòng trống
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
    /*Tạm dừng chương trình, yêu cầu người dùng nhấn Enter để tiếp tục */
    public static void pause(Scanner sc) {
        System.out.println("\nNhấn Enter để tiếp tục...");
        sc.nextLine();
    }
    /**In ra một tiêu đề lớn với gạch ngang bên dưới */
    public static void printHeader(String title) {
        System.out.println("========== " + title + " ==========");
    }
    /** In một đường gạch ngang với độ dài tùy chọn */
    public static void printLine(int length){
        for(int i = 0; i < length; i++) {
            System.out.print("-");
        }
        System.out.println();
    }
    /*Yêu cầu xác nhận từ người dùng (y/n) */
    public static boolean confirm(Scanner sc, String message){
        System.out.print(message + " (y/n): ");
        while(true){
            String input = sc.nextLine().trim().toLowerCase();
            if (input.equals("y")) return true;
            if (input.equals("n")) return false;
            System.out.print("Hãy nhập 'y' hoặc 'n': ");
        }
    }
    /** Đọc 1 dòng text. */
    public static String readLine(Scanner sc, String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    /** Đọc số nguyên trong khoảng [min, max]. */
    public static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(s);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) {}
            System.out.printf("Giá trị không hợp lệ. Vui lòng nhập số từ %d đến %d.%n", min, max);
        }
    }
}
