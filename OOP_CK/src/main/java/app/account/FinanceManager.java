package app.account;

import app.store.DataStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FinanceManager (Facade) – lớp điều phối cho UI:
 *  - tài khoản
 *  - Ghi thu/chi
 *  - Chuyển khoản nội bộ
 *  - Cung cấp báo cáo
 *
 * Dữ liệu tài khoản được lưu trong List<Map<...>> để dễ thao tác bằng Stream API.
 */
public class FinanceManager {
    // accountStore giữ bản Map bất biến (dễ stream), accountIndex cung cấp view O(1) cho đối tượng Account.
    private final List<Map<String, Object>> accountStore = new ArrayList<>();
    private final Map<String, Account> accountIndex = new LinkedHashMap<>();
    private final Ledger ledger = new Ledger();
    private final TransferService transferService = new TransferService(ledger, this::findAccount);
    private final ReportService reportService = new ReportService(ledger);

    /** Thêm tài khoản (tên phải duy nhất). */
    public String addAccount(Account account) {
        if (account == null) throw new IllegalArgumentException("Thiếu thông tin tài khoản");

        boolean duplicated = accountStore.stream() // Stream API giúp kiểm tra trùng tên nhanh gọn.
                .anyMatch(row -> Objects.equals(row.get(Account.KEY_NAME), account.getName()));
        if (duplicated) throw new IllegalArgumentException("Tên tài khoản phải là duy nhất");

        accountStore.add(account.asMap()); // Map bất biến lưu để query/report.
        accountIndex.put(account.getId(), account); // Bản đối tượng phục vụ nghiệp vụ (rút/nạp...).
        syncAccountToStore(account);
        return account.getId();
    }

    /** Đổi tên tài khoản. */
    public void updateAccountName(String id, String newName) {
        Account account = findAccountOrThrow(id); // Lấy đối tượng thật để cập nhật số dư.
        account.rename(newName);
        syncAccountToStore(account);
    }

    /** Xoá tài khoản (chỉ khi số dư = 0). */
    public void deleteAccount(String id) {
        Account account = findAccountOrThrow(id);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalStateException("Không thể xoá tài khoản khi còn số dư");

        accountIndex.remove(id);
        accountStore.removeIf(row -> Objects.equals(row.get(Account.KEY_ID), id));
        DataStore.removeAccountById(id);
    }

    /** Ghi nhận khoản thu (và trả về giao dịch tương ứng) cho 1 tài khoản. */
    public Transaction addIncome(String id, BigDecimal amount, Instant when, String note) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Số tiền thu phải > 0");
        Account account = findAccountOrThrow(id);
        account.deposit(amount, when);
        Transaction entry = Transaction.builder()
                .accountId(account.getId()).type(TxnType.INCOME)
                .amount(amount)
                .occurredAt(when).note(note)
                .build();
        ledger.record(entry);
        syncAccountToStore(account);
        return entry;
    }

    /** Ghi nhận khoản chi (và trả về giao dịch tương ứng) cho 1 tài khoản. */
    public Transaction addExpense(String id, BigDecimal amount, Instant when, String note) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Số tiền chi phải > 0");
        Account account = findAccountOrThrow(id);
        account.withdraw(amount, when);
        Transaction entry = Transaction.builder()
                .accountId(account.getId()).type(TxnType.EXPENSE)
                .amount(amount)
                .occurredAt(when).note(note)
                .build();
        ledger.record(entry);
        syncAccountToStore(account);
        return entry;
    }

    /** Chuyển tiền giữa 2 tài khoản nội bộ và đồng bộ lại DataStore. */
    public void transfer(String src, String dst, BigDecimal amount, Instant when, String note) {
        transferService.transfer(src, dst, amount, when, note);
        syncAccountToStore(findAccountOrThrow(src));
        syncAccountToStore(findAccountOrThrow(dst));
    }

    /** Danh sách tài khoản (read-only). */
    public Collection<Account> listAccounts() {
        // Duyệt danh sách map và truy ngược về đối tượng Account thực tế để trả về view bất biến.
        List<Account> list = accountStore.stream()
                .map(row -> accountIndex.get(row.get(Account.KEY_ID)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return Collections.unmodifiableList(list);
    }

    public ReportService getReportService() { return reportService; }
    public Ledger getLedger() { return ledger; }

    /** Lấy Account theo id, ném lỗi nếu không tồn tại. */
    public Account requireAccount(String id) { return findAccountOrThrow(id); }

    private Account findAccount(String id) { return accountIndex.get(id); }

    private Account findAccountOrThrow(String id) {
        return Optional.ofNullable(findAccount(id))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + id));
    }

    /** Đồng bộ dữ liệu tài khoản sang DataStore dưới dạng Map để exporter sử dụng. */
    private void syncAccountToStore(Account account) {
        if (account == null) return;
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(DataStore.AccountFields.ID, account.getId());
        row.put(DataStore.AccountFields.NAME, account.getName());
        row.put(DataStore.AccountFields.TYPE, resolveAccountType(account));
        row.put(DataStore.AccountFields.BALANCE, account.getBalance().toPlainString());
        row.put(DataStore.AccountFields.NOTE, "");
        DataStore.upsertAccount(row);
        DataStore.updateTransactionsAccountName(account.getId(), account.getName());
    }

    private String resolveAccountType(Account account) {
        return account.getType();
    }
}
