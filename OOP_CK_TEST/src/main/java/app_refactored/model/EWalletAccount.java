package app.model;
/** Tài khoản Ví điện tử (Momo, ZaloPay, ShopeePay, ...). */
public class EWalletAccount extends Account {
    public EWalletAccount(String name) {
        super(name, "Ví điện tử");
    }
}
