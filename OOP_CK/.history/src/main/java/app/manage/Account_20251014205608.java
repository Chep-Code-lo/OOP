package app.manage;
import java.math.BigDecimal;
import java.util.Objects;

public class Account {
    private final String name;
    private BigDecimal balance;

    public Account(String name, BigDecimal opening) {
        this.name = Objects.requireNonNull(name, "name");

        if (opening == null) this.balance = BigDecimal.ZERO;
        else this.balance = opening;
    }

    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }


    public void deposit(BigDecimal amount) { balance = balance.add(amount); }
    public void withdraw(BigDecimal amount) { balance = balance.subtract(amount); }

    @Override
    public String toString() {
        return name + " | Số dư: " + balance.toPlainString();
    }
}
