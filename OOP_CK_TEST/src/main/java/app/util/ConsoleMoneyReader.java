package app.util;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Scanner;

/**
 * Đối tượng chuyên trách việc đọc số tiền hợp lệ từ console.
 * Tách khỏi tầng domain để giữ lớp Account thuần nghiệp vụ.
 */
public final class ConsoleMoneyReader {
    private final Scanner scanner;

    public ConsoleMoneyReader(Scanner scanner) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
    }

    /**
     * Đọc số tiền dương từ người dùng. Chỉ chấp nhận chữ số, không dấu phân cách.
     * Thông báo lỗi và yêu cầu nhập lại tới khi hợp lệ.
     */
    public BigDecimal readAmount(String prompt) {
        System.out.println("(Chỉ nhập số nguyên > 0; KHÔNG dùng dấu chấm/phẩy/khoảng trắng)");
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("Không được để trống.");
                continue;
            }
            if (!text.chars().allMatch(Character::isDigit)) {
                System.out.println("Chỉ nhập số (0–9).");
                continue;
            }
            try {
                BigDecimal value = new BigDecimal(text);
                if (value.signum() > 0) {
                    return value;
                }
                System.out.println("Số tiền phải > 0.");
            } catch (NumberFormatException ex) {
                System.out.println("Số tiền không hợp lệ.");
            }
        }
    }
}
