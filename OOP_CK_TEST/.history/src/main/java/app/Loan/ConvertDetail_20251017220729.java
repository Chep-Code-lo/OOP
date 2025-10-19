package app.loan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.math.BigDecimal;
import java.text.*;
import java.util.Locale;


public class ConvertDetail {
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

    public static double parseInterest(String s) {
        if (s == null) return 0.0;
        double r = Double.parseDouble(s);
        return r;
    }


    public static BigDecimal parseAmount(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        String cleaned = s.trim()
                .replaceAll("[đĐ]|(?i)VND", "") // bỏ ký hiệu tiền nếu có
                .replaceAll("[\\s,_]", "");     // bỏ khoảng trắng, dấu gạch dưới, dấu phẩy nhóm
        // GIỮ: 0-9, dấu +/-, dấu chấm, e/E (khoa học). BỎ mọi thứ còn lại.
        cleaned = cleaned.replaceAll("[^0-9eE+\\-.]", "");
        return new BigDecimal(cleaned); // hiểu được "1.0E10"
    }
     public static LocalDate parseDate(String Date) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT);
        return LocalDate.parse(Date.trim(), f);
    }
}
