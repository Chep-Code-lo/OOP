package app.service;

import app.model.Account;
import app.model.Ledger;
import app.model.Transaction;
import app.model.TxnType;
import app.repository.DataStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /**
     * Ghi nhận một giao dịch thu/chi mới, cập nhật số dư tài khoản tương ứng rồi lưu vào Ledger + DataStore.
     */
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

        Transaction entry = Transaction.builder()
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

    /**
     * Chỉnh sửa giao dịch: điều chỉnh lại số dư theo chênh lệch rồi cập nhật cả Ledger và DataStore.
     */
    public void editTransaction(String accountId,
                                String transactionId,
                                TxnType newType,
                                BigDecimal newAmount,
                                LocalDate newDate,
                                String newCategory,
                                String newNote) {
        Transaction entry = findLedgerTransaction(accountId, transactionId);

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

    /**
     * Xoá giao dịch khỏi Ledger và DataStore đồng thời hoàn lại số dư ban đầu cho tài khoản.
     */
    public void deleteTransaction(String accountId, String transactionId) {
        Transaction entry = findLedgerTransaction(accountId, transactionId);
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

    /**
     * Lấy lịch sử giao dịch của một tài khoản theo thứ tự thời gian tăng dần.
     */
    public List<Transaction> historySorted(String accountId) {
        List<Transaction> list = ledger.listByAccount(accountId).stream()
                .sorted(Comparator.comparing(Transaction::getOccurredAt))
                .collect(Collectors.toList());
        return list;
    }

    /** Lấy toàn bộ giao dịch của mọi tài khoản, sắp xếp theo thời gian. */
    public List<Transaction> historyAllSorted() {
        return financeManager.listAccounts().stream()
                .flatMap(acc -> ledger.listByAccount(acc.getId()).stream())
                .sorted(Comparator.comparing(Transaction::getOccurredAt))
                .collect(Collectors.toList());
    }

    /** Truy vấn số dư hiện tại của tài khoản. */
    public BigDecimal getBalance(String accountId) {
        return financeManager.requireAccount(accountId).getBalance();
    }

    /** Lấy tên tài khoản (phục vụ hiển thị). */
    public String resolveAccountName(String accountId) {
        try {
            return financeManager.requireAccount(accountId).getName();
        } catch (IllegalArgumentException ex) {
            return "(Tài khoản không còn tồn tại)";
        }
    }

    /** Tìm một giao dịch cụ thể trong Ledger theo accountId + transactionId. */
    private Transaction findLedgerTransaction(String accountId, String txnId) {
        return ledger.find(accountId, txnId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch"));
    }

    /** Chuyển ngày LocalDate về Instant đầu ngày theo timezone hệ thống. */
    private static Instant toInstant(LocalDate date) {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    /** Trả về số có dấu dựa trên loại giao dịch (thu = dương, chi = âm). */
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
