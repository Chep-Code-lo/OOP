package app.transaction;

import app.account.*;
import app.store.DataStore;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/** Nghiệp vụ giao dịch thu/chi, tận dụng tài khoản thực tế trong FinanceManager. */
public class TransactionService {
    private final FinanceManager financeManager;
    private final Ledger ledger;

    public TransactionService(FinanceManager financeManager) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.ledger = financeManager.getLedger();
    }

    /** Danh sách tài khoản đang có trong FinanceManager. */
    public List<Account> getAccounts() {
        return new ArrayList<>(financeManager.listAccounts());
    }

    public app.account.Transaction addTransaction(String accountId,
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

        app.account.Transaction entry = app.account.Transaction.builder()
                .accountId(accountId)
                .type(type)
                .amount(normalized)
                .occurredAt(when)
                .note(note)
                .category(category)
                .build();
        ledger.record(entry);

        upsertStoreTransaction(entry, account.getName()); // Đồng bộ sang DataStore (Map cho exporter).
        return entry;
    }

    public void editTransaction(String accountId,
                                String transactionId,
                                TxnType newType,
                                BigDecimal newAmount,
                                LocalDate newDate,
                                String newCategory,
                                String newNote) {
        app.account.Transaction entry = findLedgerTransaction(accountId, transactionId);

        TxnType targetType = (newType == null) ? entry.getType() : newType;
        Instant targetWhen = (newDate == null)
                ? entry.getOccurredAt()
                : toInstant(newDate);
        BigDecimal targetAmount = (newAmount == null)
                ? entry.getAmount()
                : newAmount.setScale(2, RoundingMode.HALF_UP);

        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Số tiền phải > 0");

        BigDecimal delta = signed(targetType, targetAmount)
                .subtract(signed(entry.getType(), entry.getAmount()));

        if (delta.signum() != 0) {
            Account account = financeManager.requireAccount(accountId);
            applyDelta(account,
                    delta.signum() > 0 ? TxnType.INCOME : TxnType.EXPENSE,
                    delta.abs(),
                    targetWhen);
        }

        entry.updateType(targetType);
        entry.updateAmount(targetAmount);
        entry.updateOccurredAt(targetWhen);
        if (newCategory != null) entry.updateCategory(newCategory);
        if (newNote != null) entry.updateNote(newNote);

        String accountName = financeManager.requireAccount(accountId).getName();
        upsertStoreTransaction(entry, accountName);
    }

    public void deleteTransaction(String accountId, String transactionId) {
        app.account.Transaction entry = findLedgerTransaction(accountId, transactionId);
        ledger.remove(entry);

        Account account = financeManager.requireAccount(accountId);
        BigDecimal delta = signed(entry.getType(), entry.getAmount()).negate();
        if (delta.signum() > 0) {
            applyDelta(account, TxnType.INCOME, delta, entry.getOccurredAt());
        } else if (delta.signum() < 0) {
            applyDelta(account, TxnType.EXPENSE, delta.abs(), entry.getOccurredAt());
        }

        DataStore.removeTransactionById(entry.getId());
    }

    public List<app.account.Transaction> historySorted(String accountId) {
        List<app.account.Transaction> list = ledger.listByAccount(accountId).stream()
                .sorted(Comparator.comparing(app.account.Transaction::getOccurredAt))
                .collect(Collectors.toList());
        return list;
    }

    public BigDecimal getBalance(String accountId) {
        return financeManager.requireAccount(accountId).getBalance();
    }

    public String resolveAccountName(String accountId) {
        return financeManager.requireAccount(accountId).getName();
    }

    private app.account.Transaction findLedgerTransaction(String accountId, String txnId) {
        return ledger.find(accountId, txnId)
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
    private void upsertStoreTransaction(app.account.Transaction tx, String accountName) {
        Map<String, Object> row = new LinkedHashMap<>();
        LocalDate date = LocalDate.ofInstant(tx.getOccurredAt(), ZoneId.systemDefault());
        row.put(DataStore.TransactionFields.ID, tx.getId());
        row.put(DataStore.TransactionFields.DATE, date.toString());
        row.put(DataStore.TransactionFields.TYPE, tx.getType().name());
        row.put(DataStore.TransactionFields.AMOUNT, tx.getAmount().toPlainString());
        row.put(DataStore.TransactionFields.ACCOUNT_ID, tx.getAccountId());
        row.put(DataStore.TransactionFields.ACCOUNT_NAME, accountName);
        row.put(DataStore.TransactionFields.CATEGORY, tx.getCategory());
        row.put(DataStore.TransactionFields.NOTE, tx.getNote());
        DataStore.upsertTransaction(row);
    }
}
