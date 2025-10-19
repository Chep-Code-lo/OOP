package app.loan;

import app.model.Contract;
import app.util.DateUtils;

public class ValidInfor {
    private ValidInfor() {}

    public static boolean isValidPhone10(String s){
        return s != null && s.matches("^0\\d{9}$");
    }

    public static boolean isNonNegative(double v){
        return v >= 0;
    }

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
