package app.loan;

import java.util.Scanner;

/** Tiện ích đọc dữ liệu kiểu số và chuỗi phục vụ module vay. */
public final class ReadInfor {
    private ReadInfor() {}

    /** Đọc số thực không âm từ console (cho phép dùng dấu phẩy). */
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

    /** Đọc chuỗi không rỗng (loại bỏ khoảng trắng đầu/cuối). */
    public static String readNonBlank(Scanner sc, String prompt){
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isBlank()) return s;
            System.out.println("Không được để trống. Nhập lại!");
        }
    }
}
