package app.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;


/**
 * Input – gom logic đọc/kiểm tra dữ liệu từ terminal.
 * - CHỈ chấp nhận số tiền > 0 (0 hoặc âm bị từ chối).
 * - Chuẩn hoá về 2 lẻ (HALF_UP) để ổn định hiển thị/tính toán.
 */
public class Input {
    private final Scanner sc;

    public Input(Scanner sc) { this.sc = sc; }

    /** Đọc một dòng và trim khoảng trắng. */
    public String line(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /**
     * Nhập số tiền (> 0):
     * - <= 0 -> báo lỗi và yêu cầu nhập lại.
     * - Trả về BigDecimal đã setScale(2, HALF_UP) (ví dụ 12.345 -> 12.35).
     */
    public BigDecimal amount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = sc.nextLine();
            if (raw == null) raw = "";
            raw = raw.trim();
            if (raw.isEmpty()) {
                System.out.println("Vui lòng nhập số tiền.");
                continue;
            }

            try {
                String normalized = normalizeMoney(raw);
                BigDecimal v = new BigDecimal(normalized);
                if (v.signum() <= 0) {
                    System.out.println("Số tiền phải > 0. Nhập lại.");
                    continue;
                }
                return v.setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                System.out.println("Chỉ dùng dấu chấm '.' làm thập phân. Ví dụ hợp lệ: 1234.56");
            }
        }
    }

    /** Chuẩn: chỉ dấu '.' là thập phân. Không hỗ trợ tách nghìn.
     *  - Nếu chỉ có ',' (không có '.') và pattern dạng số, coi như thập phân => chuyển ',' -> '.'
     *  - Nếu có cả ',' và '.', hoặc dùng ',' cho tách nghìn => báo lỗi
     */
    private String normalizeMoney(String s) {
        s = s.replaceAll("\\s+", "");              // bỏ mọi khoảng trắng
        boolean hasComma = s.contains(",");
        boolean hasDot   = s.contains(".");

        if (hasComma && hasDot) {
            throw new NumberFormatException("Mix ',' và '.'");
        }

        if (hasComma) {
            // Chấp nhận như '1234,56' (một dấu phẩy, có số ở hai bên) -> coi ',' là thập phân
            if (s.matches("\\d+,\\d+")) {
                System.out.println("Bạn dùng ',' làm thập phân -> mình chuyển sang '.' theo chuẩn.");
                return s.replace(',', '.');
            }
            // Các kiểu còn lại với ',' (vd: 1,234 hoặc 1,234,567) => KHÔNG hỗ trợ tách nghìn
            throw new NumberFormatException("Không hỗ trợ dấu phẩy phân tách nghìn.");
        }

        // Không có dấu phẩy: cho phép số nguyên hoặc số có 1 dấu chấm (BigDecimal sẽ kiểm tra chi tiết)
        return s;
    }
}
