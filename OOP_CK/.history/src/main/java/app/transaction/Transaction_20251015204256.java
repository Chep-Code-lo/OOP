package app.transaction;

import app.account.TxnType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Giao dịch thu/chi cho một tài khoản (tham chiếu tài khoản qua id). */
public class Transaction {
    public final String id;
    public final String accountId;
    public TxnType type;
    public BigDecimal amount;
    public LocalDate date;
    public String category;
    public String note;

    public Transaction(String accountId,
                       TxnType type,
                       BigDecimal amount,
                       LocalDate date,
                       String category,
                       String note) {
        this(UUID.randomUUID().toString(), accountId, type, amount, date, category, note);
    }

    public Transaction(String id,
                       String accountId,
                       TxnType type,
                       BigDecimal amount,
                       LocalDate date,
                       String category,
                       String note) {
        this.id = Objects.requireNonNull(id, "id");
        this.accountId = Objects.requireNonNull(accountId, "accountId");
        this.type = Objects.requireNonNull(type, "type");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.date = Objects.requireNonNull(date, "date");
        this.category = (category == null) ? "" : category;
        this.note = (note == null) ? "" : note;
    }

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public String toString() {
        String sign = (type == TxnType.INCOME) ? "+" : "-";
        return date.format(DMY) + " | " + sign + amount.toPlainString()
                + " | " + type
                + (category.isEmpty() ? "" : " | #" + category)
                + (note.isEmpty() ? "" : " | " + note);
    }
}

