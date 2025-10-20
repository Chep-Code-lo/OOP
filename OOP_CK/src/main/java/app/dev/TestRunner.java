package app.dev;

import app.export.ExportAccounts;
import app.export.ExportTransactions;
import app.model.BankAccount;
import app.model.EWalletAccount;
import app.model.TxnType;
import app.service.FinanceManager;
import app.service.TransactionService;
import app.repository.DataStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Bộ kiểm tra chức năng ở mức dịch vụ: tạo tài khoản, chuyển tiền, ghi giao dịch,
 * đồng bộ DataStore và chạy exporter. Ném AssertionError khi phát hiện sai khác.
 */
public final class TestRunner {
    private TestRunner() {}

    public static void main(String[] args) throws Exception {
        DataStore.clearAll();

        FinanceManager fm = new FinanceManager();
        String bankId = fm.addAccount(new BankAccount("BankTest"));
        String walletId = fm.addAccount(new EWalletAccount("WalletTest"));

        fm.addIncome(bankId, new BigDecimal("1500"), Instant.now(), "Bonus");
        fm.transfer(bankId, walletId, new BigDecimal("500"), Instant.now(), "Move");

        requireEquals(fm.requireAccount(bankId).getBalance(), new BigDecimal("1000.00"),
                "Bank account balance after transfer");
        requireEquals(fm.requireAccount(walletId).getBalance(), new BigDecimal("500.00"),
                "Wallet balance after receiving transfer");

        TransactionService ts = new TransactionService(fm);
        ts.addTransaction(walletId, TxnType.EXPENSE, new BigDecimal("120"), LocalDate.now(), "Food", "Test expense");
        requireEquals(ts.getBalance(walletId), new BigDecimal("380.00"),
                "Wallet balance after recording expense");

        // ===== Negative tests =====
        expectThrows(IllegalStateException.class, () ->
                ts.addTransaction(walletId, TxnType.EXPENSE, new BigDecimal("1000"),
                        LocalDate.now(), "Overspend", "Should fail"));
        requireEquals(ts.getBalance(walletId), new BigDecimal("380.00"),
                "Wallet balance unchanged after failed expense");

        expectThrows(IllegalStateException.class, () ->
                fm.transfer(walletId, bankId, new BigDecimal("1000"),
                        Instant.now(), "Overdraft transfer"));
        requireEquals(fm.requireAccount(walletId).getBalance(), new BigDecimal("380.00"),
                "Wallet balance unchanged after failed transfer");

        requireTrue(DataStore.transactions().size() >= 1, "DataStore should contain at least one transaction");

        ExportAccounts.export();
        ExportTransactions.export();

        DataStore.clearAll();
        System.out.println("All functional tests passed.");
    }

    /** So sánh hai giá trị BigDecimal và ném AssertionError nếu khác nhau. */
    private static void requireEquals(BigDecimal actual, BigDecimal expected, String message) {
        if (actual.compareTo(expected) != 0) {
            throw new AssertionError(message + " | expected=" + expected + ", actual=" + actual);
        }
    }

    /** Khẳng định điều kiện đúng; sai -> ném AssertionError. */
    private static void requireTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /** Đảm bảo block ném đúng loại ngoại lệ mong đợi. */
    private static void expectThrows(Class<? extends Throwable> type, Runnable block) {
        try {
            block.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("Expected exception " + type.getSimpleName()
                    + " but caught " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError("Expected exception " + type.getSimpleName() + " but nothing was thrown");
    }
}
