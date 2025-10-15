package app.loan;

import java.util.Scanner;

/** Helper đọc dữ liệu từ console với thông báo lỗi thân thiện. */
public final class ConsoleIO{
    private ConsoleIO() {}

    /** Đọc số thực >= 0, tự động báo lỗi và yêu cầu nhập lại. */
    public static double readPositiveDouble(Scanner sc, String prompt){
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim().replace(',', '.');
            try {
                double v = Double.parseDouble(line);
                if (v >= 0) return v;
                System.out.println("Giá trị phải ≥ 0. Nhập lại!");
            } catch (NumberFormatException e) {
                System.out.println("Không phải số hợp lệ. Nhập lại!");
            }
        }
    }

    /** Đọc chuỗi không rỗng (trim khoảng trắng). */
    public static String readNonBlank(Scanner sc, String prompt){
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isBlank()) return s;
            System.out.println("Không được để trống. Nhập lại!");
        }
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
