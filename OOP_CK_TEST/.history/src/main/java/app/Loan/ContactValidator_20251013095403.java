public class ContactValidator {
    private ContactValidator() {}

    public static boolean isValidPhone10(String s){
        return s != null && s.matches("^0\\d{9}$");
    }

    public static boolean isNonNegative(double v){
        return v >= 0;
    }

    public static void validate(Contact c){
        if (c.getName() == null || c.getName().isBlank())
            throw new IllegalArgumentException("Tên không được để trống");
        if (!isNonNegative(c.getMoney()))
            throw new IllegalArgumentException("Số tiền phải ≥ 0");
        if (!isValidPhone10(c.getPhoneNumber()))
            throw new IllegalArgumentException("Số điện thoại phải đúng 10 chữ số");
        if (!DateUtils.isValidDDMMYY(c.getDueDate()))
            throw new IllegalArgumentException("Ngày hạn (DD/MM/YY) không hợp lệ");
        if (!isNonNegative(c.getInterest()))
            throw new IllegalArgumentException("Lãi suất phải ≥ 0");
    }
}
