package app.transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class TransactionService {
    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, List<Transaction>> ledger   = new HashMap<>();
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts.values());
    }


    public void addAccount(String name, BigDecimal opening) {
        if (name == null) throw new IllegalArgumentException("name null");
        if (accounts.containsKey(name)) throw new IllegalArgumentException("Tài khoản đã tồn tại");
        accounts.put(name, new Account(name, opening));
        ledger.put(name, new ArrayList<>());
    }

    public void addTransaction(String accountName, TxType type, BigDecimal amount,LocalDate date, String category, String note) {
        Account acc = accounts.get(accountName);
        List<Transaction> list = ledger.get(accountName);
        if (acc == null || list == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
        if (type == null || amount == null || date == null) throw new IllegalArgumentException("Thiếu dữ liệu");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Số tiền phải > 0");

        if (category == null) category = "";
        if (note == null) note = "";

        list.add(new Transaction(accountName, type, amount, date, category, note));

    }

    public void editTransaction(String accountName, int index, TxType newType, BigDecimal newAmount,
                                LocalDate newDate, String newCategory, String newNote) {
        List<Transaction> list = ledger.get(accountName);
        if (list == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
        if (index < 0 || index >= list.size()) throw new IllegalArgumentException("STT không hợp lệ");
        if (newType == null || newAmount == null || newDate == null) throw new IllegalArgumentException("Thiếu dữ liệu");
        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Số tiền phải > 0");

        Transaction t = list.get(index);
        t.type = newType;
        t.amount = newAmount;
        t.date = newDate;
        t.category = (newCategory == null ? "" : newCategory);
        t.note = (newNote == null ? "" : newNote);

    }

    public void deleteTransaction(String accountName, int index) {
        List<Transaction> list = ledger.get(accountName);
        if (list == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
        if (index < 0 || index >= list.size()) throw new IllegalArgumentException("STT không hợp lệ");
        list.remove(index);

    }

    // Lịch sử theo ngày
    public List<Transaction> historySorted(String accountName) {
        List<Transaction> list = ledger.get(accountName);
        if (list == null) throw new IllegalArgumentException("Tài khoản không tồn tại");
        List<Transaction> copy = new ArrayList<>(list);
        Collections.sort(copy, new Comparator<Transaction>() {
            @Override public int compare(Transaction a, Transaction b) {
                return a.date.compareTo(b.date);
            }
        });
        return copy;
    }


    public BigDecimal getBalance(String accountName) {
        Account acc = accounts.get(accountName);
        List<Transaction> list = ledger.get(accountName);
        if (acc == null || list == null) throw new IllegalArgumentException("Tài khoản không tồn tại");

        BigDecimal total = acc.getBalance();
        for (Transaction t : list) {
            if (t.type == TxType.INCOME) total = total.add(t.amount);
            else total = total.subtract(t.amount);
        }
        return total;
    }
}
