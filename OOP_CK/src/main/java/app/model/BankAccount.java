package app.model;

public class BankAccount extends Account {
    /** Khởi tạo tài khoản ngân hàng với tên do người dùng nhập. */
    public BankAccount(String name) {
        super(name, "Ngân hàng");
    }
}
