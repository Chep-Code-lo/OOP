package app.loan;
import java.math.BigDecimal;


/** Chuyển đổi chuỗi thông tin hợp đồng thành dạng chuẩn để tính lãi. */
public class ConvertDetail {
    /** Diễn giải chuỗi loại lãi thành mô tả tiếng Việt (và kiểm tra % lãi). */
    public static String parseType(String s, double interestPercent) {
        if (interestPercent <= 0) return "không tính lãi";
        if (s == null) return "không tính lãi";

        // làm sạch: bỏ mọi dấu "
        String v = s.replace("\"", "").trim().toUpperCase();

        switch (v) {
            case "SIMPLE":   return "lãi đơn";
            case "COMPOUND": return "lãi kép";
            default:         return "không tính lãi";
        }
    }

    /** Chuyển chuỗi lãi suất thành giá trị double (mặc định 0 nếu null). */
    public static double parseInterest(String s) {
        if (s == null) return 0.0;
        double r = Double.parseDouble(s);
        return r;
    }


    /** Làm sạch chuỗi số tiền và chuyển về BigDecimal. */
    public static BigDecimal parseAmount(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String cleaned = s.trim()
                .replaceAll("[đĐ]|(?i)VND", "") // bỏ ký hiệu tiền nếu có
                .replaceAll("[\\s,_]", "");     // bỏ khoảng trắng, dấu gạch dưới, dấu phẩy nhóm
        // GIỮ: 0-9, dấu +/-, dấu chấm, e/E (khoa học). BỎ mọi thứ còn lại.
        cleaned = cleaned.replaceAll("[^0-9eE+\\-.]", "");
        return new BigDecimal(cleaned); // hiểu được "1.0E10"
    }
}
