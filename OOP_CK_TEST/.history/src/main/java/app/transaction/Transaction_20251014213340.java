package app.transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
public class Transaction {
    public String accountName;
    public TxType type;
    public BigDecimal amount;
    public LocalDate date;
    public String category;
    public String note;

    public Transaction(String accountName,  TxType type, BigDecimal amount, LocalDate date, String category, String note ) {
        this.accountName = accountName;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.category = (category == null ? "" : category);
        this.note = (note == null ? "" : note);
    }
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    @Override
    public String toString() {
        String sign = (type == TxType.INCOME) ? "+" : "-";
        return date.format(DMY) + " | " + sign + amount.toPlainString()
                + " | " + type
                + (category.isEmpty() ? "" : " | #" + category)
                + (note.isEmpty() ? "" : " | " + note);
    }
}

