package app;

import app.account.AccountMenu;
import app.account.FinanceManager;
import app.account.ReportMenu;
import app.account.TransferMenu;
import app.account.TxnType;
import app.export.ExportAccounts;
import app.export.ExportLoans;
import app.export.ExportTransactions;
import app.loan.ContractStorage;
import app.store.DataStore;
import app.transaction.TransactionService;
import app.ui.ExportMenu;
import app.ui.MenuLoan;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Kịch bản kiểm thử automation cho toàn bộ luồng chính của ứng dụng.
 * Mục tiêu: kiểm tra nhanh các nhánh nghiệp vụ, đồng thời chờ ngắn để quan sát log.
 */
public final class TestScript {
    private static final long DELAY_MS = 800L;

    private TestScript() {}

    public static void main(String[] args) throws Exception {
        FinanceManager fm = new FinanceManager();
        TransactionService txService = new TransactionService(fm);

        String[] bankId = new String[1];
        String[] walletId = new String[1];
        String[] transactionId = new String[1];
        List<String> failed = new ArrayList<>();

        System.out.println("\n=== BẮT ĐẦU SCRIPT KIỂM THỬ ===");

        runStep("Tạo tài khoản BankDemo qua menu", failed, () -> {
            runWithInput("""
                    1
                    1
                    BankDemo
                    n

                    0
                    """, () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
            bankId[0] = findAccountIdByName(fm, "BankDemo");
            ensure(bankId[0] != null, "Không lấy được ID BankDemo");
        });

        runStep("Tạo tài khoản WalletDemo qua menu", failed, () -> {
            runWithInput("""
                    1
                    2
                    WalletDemo
                    n

                    0
                    """, () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
            walletId[0] = findAccountIdByName(fm, "WalletDemo");
            ensure(walletId[0] != null, "Không lấy được ID WalletDemo");
        });

        runStep("Nạp 3000 vào BankDemo", failed, () -> {
            ensure(bankId[0] != null, "Chưa có tài khoản BankDemo");
            fm.addIncome(bankId[0], new BigDecimal("3000"), Instant.now(), "Initial income");
            ensure(fm.requireAccount(bankId[0]).getBalance().compareTo(new BigDecimal("3000")) == 0,
                    "Số dư sau khi nạp không đúng");
        });

        runStep("Chuyển 1000 từ BankDemo sang WalletDemo", failed, () -> {
            ensure(bankId[0] != null && walletId[0] != null, "Thiếu tài khoản để chuyển");
            runWithInput(String.format("""
                    1
                    %s
                    %s
                    1000
                    Chuyen sang vi
                    n
                    0
                    """, bankId[0], walletId[0]), () -> new TransferMenu(fm, new Scanner(System.in)).showMenu());
            ensure(fm.requireAccount(bankId[0]).getBalance().compareTo(new BigDecimal("2000")) == 0,
                    "Số dư tài khoản nguồn sau chuyển chưa đúng");
            ensure(fm.requireAccount(walletId[0]).getBalance().compareTo(new BigDecimal("1000")) == 0,
                    "Số dư tài khoản đích sau chuyển chưa đúng");
        });

        runStep("Ghi giao dịch chi 250 từ WalletDemo", failed, () -> {
            ensure(walletId[0] != null, "Chưa có tài khoản WalletDemo");
            var tx = txService.addTransaction(walletId[0], TxnType.EXPENSE, new BigDecimal("250"),
                    LocalDate.of(2025, 1, 1), "An uong", "Chi demo");
            transactionId[0] = tx.getId();
            ensure(fm.requireAccount(walletId[0]).getBalance().compareTo(new BigDecimal("750")) == 0,
                    "Số dư sau giao dịch chi không khớp");
        });

        runStep("Xem lịch sử giao dịch (CLI)", failed, () -> {
            app.transaction.App txApp = new app.transaction.App(txService);
            runWithInput("1\n\n", () -> txApp.listTxCLI(new Scanner(System.in)));
        });

        runStep("Sửa giao dịch thành chi 200", failed, () -> {
            ensure(transactionId[0] != null, "Thiếu giao dịch để sửa");
            app.transaction.App txApp = new app.transaction.App(txService);
            runWithInput("""
                    1
                    1
                    2
                    200
                    01/02/2025
                    Sinh hoat
                    Ghi chu cap nhat
                    
                    """, () -> txApp.editTxCLI(new Scanner(System.in)));
            ensure(fm.requireAccount(walletId[0]).getBalance().compareTo(new BigDecimal("800")) == 0,
                    "Số dư sau khi chỉnh giao dịch chưa đúng");
        });

        runStep("Xóa giao dịch vừa tạo", failed, () -> {
            ensure(transactionId[0] != null, "Thiếu giao dịch để xóa");
            app.transaction.App txApp = new app.transaction.App(txService);
            runWithInput("1\n1\n", () -> txApp.deleteTxCLI(new Scanner(System.in)));
            ensure(fm.requireAccount(walletId[0]).getBalance().compareTo(new BigDecimal("1000")) == 0,
                    "Số dư sau khi xóa giao dịch chưa trở lại 1000");
        });

        runStep("Báo cáo số dư", failed, () -> new ReportMenu(fm, new Scanner(System.in)).showBalances());

        runStep("Tạo hợp đồng vay qua MenuLoan", failed, () -> {
            String script = """
                    1
                    1
                    Nguoi vay A
                    5000
                    0911222333
                    01/01/2025
                    01/06/2025
                    10
                    1
                    Ghi chu ban dau
                    3

                    0
                    """;
            runWithInput(script, () -> new MenuLoan().showMenu());
            ensure(!DataStore.loans().isEmpty(), "Loan chưa được lưu vào DataStore");
        });

        runStep("Cập nhật hợp đồng vay", failed, () -> {
            String script = """
                    2
                    1
                    Nguoi vay A (update)
                    
                    
                    
                    
                    12
                    COMPOUND
                    Ghi chu sau cap nhat

                    0
                    """;
            runWithInput(script, () -> new MenuLoan().showMenu());
            ensure(DataStore.loans().stream()
                    .anyMatch(row -> "Nguoi vay A (update)".equals(row.get(DataStore.LoanFields.NAME))),
                    "Tên hợp đồng chưa được cập nhật");
        });

        runStep("Xem ngày đến hạn hợp đồng", failed, () -> {
            String script = """
                    4
                    1

                    0
                    """;
            runWithInput(script, () -> new MenuLoan().showMenu());
        });

        runStep("Tính lãi hợp đồng", failed, () -> {
            String script = """
                    5
                    1

                    0
                    """;
            runWithInput(script, () -> new MenuLoan().showMenu());
        });

        runStep("Xóa hợp đồng vay", failed, () -> {
            String script = """
                    3
                    1
                    y

                    0
                    """;
            runWithInput(script, () -> new MenuLoan().showMenu());
            ensure(DataStore.loans().isEmpty(), "Loan vẫn còn trong DataStore sau khi xóa");
        });

        runStep("Flush hợp đồng ra CSV", failed, () -> {
            int flushed = ContractStorage.flushPending();
            ensure(flushed >= 0, "Flush không thành công");
        });

        runStep("Xuất CSV thủ công", failed, () -> {
            ExportAccounts.export();
            ExportTransactions.export();
            ExportLoans.export();
        });

        runStep("Menu Export (chọn lưu tất cả)", failed,
                () -> runWithInput("5\n\n0\n", () -> new ExportMenu(new Scanner(System.in)).show()));

        runStep("Kiểm tra file CSV tạo ra", failed, () -> {
            ensure(Files.exists(Paths.get("data", "accounts.csv")), "Thiếu file accounts.csv");
            ensure(Files.exists(Paths.get("data", "transactions.csv")), "Thiếu file transactions.csv");
            ensure(Files.exists(Paths.get("data", "loans.csv")), "Thiếu file loans.csv");
        });

        if (failed.isEmpty()) {
            System.out.println("\n=== HOÀN TẤT: TẤT CẢ BƯỚC THÀNH CÔNG ===");
        } else {
            System.out.println("\n=== HOÀN TẤT: " + failed.size() + " BƯỚC THẤT BẠI ===");
            failed.forEach(name -> System.out.println(" - " + name));
            throw new IllegalStateException("Một hoặc nhiều bước kiểm thử thất bại");
        }
    }

    private static void runStep(String name, List<String> failed, CheckedRunnable action) {
        System.out.println("\n--- " + name + " ---");
        try {
            action.run();
            System.out.println("[PASS] " + name);
        } catch (Throwable t) {
            System.out.println("[FAIL] " + name + ": " + t.getMessage());
            t.printStackTrace(System.out);
            failed.add(name);
        } finally {
            try {
                Thread.sleep(DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void runWithInput(String text, CheckedRunnable action) throws Exception {
        InputStream original = System.in;
        try {
            System.setIn(new ByteArrayInputStream(text.getBytes()));
            action.run();
        } finally {
            System.setIn(original);
        }
    }

    private static String findAccountIdByName(FinanceManager fm, String name) {
        return fm.listAccounts().stream()
                .filter(a -> Objects.equals(a.getName(), name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + name))
                .getId();
    }

    private static void ensure(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
