package app.model;
/** Tài khoản Ví điện tử (Momo, ZaloPay, ShopeePay, ...). */
public class EWalletAccount extends Account {
    /** Khởi tạo tài khoản ví điện tử với tên do người dùng nhập. */
    public EWalletAccount(String name) {
        super(name, "Ví điện tử");
    }
}
