package app.transaction;

import app.account.*;
import app.store.DataStore;
import java.math.*;
import java.time.*;
import java.util.*;

/** Nghiệp vụ giao dịch thu/chi, tận dụng tài khoản thực tế trong FinanceManager. */
public class TransactionService {
    private final FinanceManager financeManager;
    // Lưu lịch sử giao dịch theo từng tài khoản bằng List<Transaction> (Transaction đã bọc dữ liệu Map).
    private final Map<String, List<Transaction>> ledger = new HashMap<>();

    public TransactionService(FinanceManager financeManager) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
    }

    /** Danh sách tài khoản đang có trong FinanceManager. */
    public List<Account> getAccounts() {
        return new ArrayList<>(financeManager.listAccounts());
    }

    public Transaction addTransaction(String accountId,
                                      TxnType type,
                                      BigDecimal amount,
                                      LocalDate date,
                                      String category,
                                      String note) {
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("Thiếu tài khoản");
        if (type == null || amount == null || date == null)
            throw new IllegalArgumentException("Thiếu dữ liệu");
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền phải > 0");

        Account account = financeManager.requireAccount(accountId); // Lấy Account thực để điều chỉnh số dư.
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        Instant when = toInstant(date);

        applyDelta(account, type, normalized, when); // Áp dụng lên số dư thực trước khi lưu sổ.

        Transaction tx = new Transaction(
                accountId,
                type,
                normalized,
                date,
                category,
                note
        );
        ledgerFor(accountId).add(tx); // Giữ lại bản Transaction phục vụ chỉnh sửa tương lai.
        upsertStoreTransaction(tx, account.getName()); // Đồng bộ sang DataStore (Map cho exporter).
        return tx;
    }

    public void editTransaction(String accountId,
                                String transactionId,
                                TxnType newType,
                                BigDecimal newAmount,
                                LocalDate newDate,
                                String newCategory,
                                String newNote) {
        Transaction tx = findTransaction(accountId, transactionId);

        TxnType targetType = (newType == null) ? tx.type : newType;
        LocalDate targetDate = (newDate == null) ? tx.date : newDate;
        BigDecimal targetAmount = (newAmount == null)
                ? tx.amount
                : newAmount.setScale(2, RoundingMode.HALF_UP);

        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền phải > 0");

        BigDecimal delta = signed(targetType, targetAmount)
                .subtract(signed(tx.type, tx.amount));

        if (delta.signum() != 0) {
            Account account = financeManager.requireAccount(accountId);
            Instant when = toInstant(targetDate);
            applyDelta(account, delta.signum() > 0 ? TxnType.INCOME : TxnType.EXPENSE,
                    delta.abs(), when);
        }

        tx.type = targetType; // Cập nhật bản Transaction in-memory để phản ánh thay đổi.
        tx.amount = targetAmount;
        tx.date = targetDate;
        tx.category = (newCategory == null) ? tx.category : newCategory;
        tx.note = (newNote == null) ? tx.note : newNote;

        String accountName = financeManager.requireAccount(accountId).getName();
        upsertStoreTransaction(tx, accountName);
    }

    public void deleteTransaction(String accountId, String transactionId) {
        List<Transaction> list = ledgerFor(accountId);
        Transaction tx = findTransaction(accountId, transactionId);
        list.remove(tx);

        Account account = financeManager.requireAccount(accountId);
        BigDecimal delta = signed(tx.type, tx.amount).negate();
        if (delta.signum() > 0) {
            applyDelta(account, TxnType.INCOME, delta, toInstant(tx.date));
        } else if (delta.signum() < 0) {
            applyDelta(account, TxnType.EXPENSE, delta.abs(), toInstant(tx.date));
        }

        DataStore.removeTransactionById(tx.id);
    }

    public List<Transaction> historySorted(String accountId) {
        List<Transaction> list = ledgerFor(accountId);
        List<Transaction> copy = new ArrayList<>(list);
        copy.sort(Comparator.comparing(t -> t.date));
        return copy;
    }

    public BigDecimal getBalance(String accountId) {
        return financeManager.requireAccount(accountId).getBalance();
    }

    public String resolveAccountName(String accountId) {
        return financeManager.requireAccount(accountId).getName();
    }

    /** Lấy (hoặc khởi tạo) danh sách giao dịch ứng với một tài khoản. */
    private List<Transaction> ledgerFor(String accountId) {
        return ledger.computeIfAbsent(accountId, k -> new ArrayList<>());
    }

    /** Tra cứu giao dịch theo id trong ledger nội bộ của tài khoản. */
    private Transaction findTransaction(String accountId, String transactionId) {
        return ledgerFor(accountId).stream()
                .filter(t -> Objects.equals(t.id, transactionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch"));
    }

    private static Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    private static BigDecimal signed(TxnType type, BigDecimal amount) {
        return (type == TxnType.INCOME) ? amount : amount.negate();
    }

    /** Áp dụng biến động số dư trực tiếp lên Account tuỳ loại thu/chi. */
    private void applyDelta(Account account, TxnType type, BigDecimal amount, Instant when) {
        if (type == TxnType.INCOME) {
            account.deposit(amount, when);
        } else if (type == TxnType.EXPENSE) {
            account.withdraw(amount, when);
        } else {
            throw new IllegalArgumentException("Chỉ hỗ trợ thu/chi");
        }
    }

    /** Đồng bộ giao dịch sang DataStore (map bất biến) để module xuất CSV sử dụng. */
    private void upsertStoreTransaction(Transaction tx, String accountName) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(DataStore.TransactionFields.ID, tx.id);
        row.put(DataStore.TransactionFields.DATE, tx.date.toString());
        row.put(DataStore.TransactionFields.TYPE, tx.type.name());
        row.put(DataStore.TransactionFields.AMOUNT, tx.amount.toPlainString());
        row.put(DataStore.TransactionFields.ACCOUNT_ID, tx.accountId);
        row.put(DataStore.TransactionFields.ACCOUNT_NAME, accountName);
        row.put(DataStore.TransactionFields.CATEGORY, tx.category);
        row.put(DataStore.TransactionFields.NOTE, tx.note);
        DataStore.upsertTransaction(row);
    }
}
