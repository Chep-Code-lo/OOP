package app.loan;

import app.model.Contract;
import app.util.DateUtils;

/** Tiện ích kiểm tra tính hợp lệ của thông tin hợp đồng vay. */
public class ValidInfor {
    private ValidInfor() {}

    /** Kiểm tra số điện thoại có đúng 10 chữ số bắt đầu bằng 0. */
    public static boolean isValidPhone10(String s){
        return s != null && s.matches("^0\\d{9}$");
    }

    /** Kiểm tra số không âm. */
    public static boolean isNonNegative(double v){
        return v >= 0;
    }

    /** Xác thực toàn bộ trường quan trọng của hợp đồng, ném lỗi nếu sai. */
    public static void validate(Contract c){
        if (c.getName() == null || c.getName().isBlank())
            throw new IllegalArgumentException("Tên không được để trống");
        if (!isValidPhone10(c.getPhoneNumber()))
            throw new IllegalArgumentException("Số điện thoại phải đúng 10 chữ số");
        if (!DateUtils.isValidDDMMYY(c.getTraDate()))
            throw new IllegalArgumentException("Ngày hạn (DD/MM/YYYY) không hợp lệ");
        if (!DateUtils.isValidDDMMYY(c.getVayDate()))
            throw new IllegalArgumentException("Ngày tạo hợp đồng (DD/MM/YYYY) không hợp lệ");
        if (!isNonNegative(c.getInterest()))
            throw new IllegalArgumentException("Lãi suất phải ≥ 0");
    }

}
