package app.loan;
import java.util.Scanner;

public final class LoanReadInfor{
    private LoanReadInfor() {}

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

    public static String readNonBlank(Scanner sc, String prompt){
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (!s.isBlank()) return s;
            System.out.println("Không được để trống. Nhập lại!");
        }
    }
}
