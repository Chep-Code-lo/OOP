package app.ui;

import java.time.LocalDate;
import java.time.format.*;
import java.util.Scanner;

/** Tiện ích chuẩn hoá/kiểm tra ngày tháng cho module khoản vay. */
public class DateUtils{
    private static final DateTimeFormatter DTF = DateTimeFormatter
    .ofPattern("dd/MM/uuuu") 
    .withResolverStyle(ResolverStyle.STRICT);
    private DateUtils() {}
    /** Kiểm tra chuỗi ngày theo định dạng dd/MM/yyyy với strict resolver. */
    public static boolean isValidDDMMYY(String s){
        try{
            LocalDate.parse(s, DTF);
            return true;
        }catch (DateTimeParseException e){
            return false;
        }
    }

    public static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Đọc ngày từ console, mặc định là hôm nay nếu bỏ trống. */
    public static LocalDate readDate(Scanner sc) {
        System.out.print("Ngày (dd/MM/yyyy, Enter=hôm nay): ");
        String s = sc.nextLine().trim();
        return s.isEmpty() ? LocalDate.now() : parseDate(s);
    }

    /** Parse chuỗi dd/MM/yyyy và ném lỗi nếu sai định dạng. */
    public static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DMY);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày sai (đúng: dd/MM/yyyy)");
        }
    }
}
