package org.example.finance.app;

import org.example.finance.common.TxnType;
import org.example.finance.core.*;
import org.example.finance.service.ReportService;
import org.example.finance.service.TransferService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * FinanceManager (Facade) – lớp điều phối cho UI:
 *  - tài khoản
 *  - Ghi thu/chi
 *  - Chuyển khoản nội bộ
 *  - Cung cấp báo cáo
 *
 * Quy ước:
 *  - Tên tài khoản phải duy nhất.
 *  - Không xoá tài khoản khi còn số dư.
 */
public class FinanceManager {
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final Ledger ledger = new Ledger();
    private final TransferService transferService = new TransferService(ledger, accounts);
    private final ReportService reportService = new ReportService(ledger);

    /** Thêm tài khoản (tên phải duy nhất). */
    public String addAccount(Account a) {
        if (a == null) throw new IllegalArgumentException("Thiếu thông tin tài khoản");
        if (accounts.values().stream().anyMatch(x -> x.getName().equals(a.getName())))
            throw new IllegalArgumentException("Tên tài khoản phải là duy nhất");
        accounts.put(a.getId(), a);
        return a.getId();
    }

    /** Đổi tên tài khoản. */
    public void updateAccountName(String id, String newName) { get(id).rename(newName); }

    /** Xoá tài khoản (chỉ khi số dư = 0). */
    public void deleteAccount(String id) {
        Account a = get(id);
        if (a.getBalance().compareTo(BigDecimal.ZERO) != 0)
            throw new IllegalStateException("Không thể xoá tài khoản khi còn số dư");
        accounts.remove(id);
    }

    public void addIncome(String id, BigDecimal amount, Instant when, String note) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Số tiền thu phải > 0");
        Account a = get(id);
        a.deposit(amount, when);
        ledger.record(Transaction.builder()
                .accountId(a.getId()).type(TxnType.INCOME)
                .amount(amount)                     // <<< QUAN TRỌNG
                .occurredAt(when).note(note)
                .build());
    }

    public void addExpense(String id, BigDecimal amount, Instant when, String note) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Số tiền chi phải > 0");
        Account a = get(id);
        a.withdraw(amount, when);
        ledger.record(Transaction.builder()
                .accountId(a.getId()).type(TxnType.EXPENSE)
                .amount(amount)                     // <<< QUAN TRỌNG
                .occurredAt(when).note(note)
                .build());
    }

    public void transfer(String src, String dst, BigDecimal amount, Instant when, String note) {
        transferService.transfer(src, dst, amount, when, note);
    }


    /** Danh sách tài khoản (read-only). */
    public Collection<Account> listAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public ReportService getReportService() { return reportService; }
    public Ledger getLedger() { return ledger; }

    /** Lấy tài khoản theo id; không thấy → lỗi. */
    private Account get(String id) {
        Account a = accounts.get(id);
        if (a == null) throw new IllegalArgumentException("Không tìm thấy tài khoản: " + id);
        return a;
    }
}
