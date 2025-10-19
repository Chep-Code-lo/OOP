package app;

import app.account.AccountMenu;
import app.account.FinanceManager;
import app.account.ReportMenu;
import app.account.TransferMenu;
import app.account.TxnType;
import app.export.ExportAccounts;
import app.export.ExportTransactions;
import app.loan.MakeContact;
import app.transaction.TransactionService;
import app.ui.MenuLoan;
import app.ui.ExportMenu;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Script tự động hoá toàn bộ luồng chức năng, xen kẽ thời gian tạm dừng
 * để người dùng quan sát kết quả trên console.
 */
public final class TestScript {
    private TestScript() {}

    private static final long PAUSE_MS = 750L;

    public static void main(String[] args) throws Exception {
        FinanceManager fm = new FinanceManager();

        System.out.println("\n=== BẮT ĐẦU TEST FULL CHỨC NĂNG ===");

        // 1. Thêm 2 tài khoản qua AccountMenu
        runWithInput("""
                1
                1
                BankDemo
                n

                0
                """, () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
        pause("Đã tạo tài khoản BankDemo");

        runWithInput("""
                1
                2
                WalletDemo
                n

                0
                """, () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
        pause("Đã tạo tài khoản WalletDemo");

        // 2. Nạp thu vào BankDemo
        var bankId = findAccountIdByName(fm, "BankDemo");
        fm.addIncome(bankId, new BigDecimal("3000"), Instant.now(), "Initial income");
        pause("Đã nạp 3000 VND vào BankDemo");

        // 3. Chuyển 1000 sang WalletDemo bằng menu chuyển khoản
        var walletId = findAccountIdByName(fm, "WalletDemo");
        runWithInput(String.format("""
                1
                %s
                %s
                1000
                Chuyển sang ví
                2
                """, bankId, walletId), () -> new TransferMenu(fm, new Scanner(System.in)).showMenu());
        pause("Đã chuyển 1000 từ BankDemo sang WalletDemo");

        // 4. Ghi một giao dịch chi thông qua TransactionService/App
        TransactionService transactionService = new TransactionService(fm);
        transactionService.addTransaction(walletId, TxnType.EXPENSE, new BigDecimal("250"),
                LocalDate.of(2025, 1, 1), "Ăn uống", "Chi demo");
        pause("Đã chi 250 từ WalletDemo");

        // 5. Hiển thị số dư và báo cáo
        new ReportMenu(fm, new Scanner(System.in)).showBalances();
        pause("Kiểm tra số dư");

        runWithInput("1\n\n0\n", () -> new ReportMenu(fm, new Scanner(System.in)).showReport());
        pause("Xem báo cáo thu chi tổng");

        // 6. Ghi một khoản vay cơ bản
        runWithInput("""
                1
                Người vay A
                5000
                0911222333
                15/05/2025
                10
                Ghi chú vay mượn
                
                3
                """, () -> new  MakeContact.showMenu(new Scanner(System.in)).show());
        pause("Nhập xong khoản vay");

        // 7. Xuất CSV
        ExportAccounts.export();
        ExportTransactions.export();
        pause("Đã xuất CSV accounts và transactions");

        // 8. Chạy menu Export để sử dụng cùng logic console (demo option 5 và quay lại)
        runWithInput("5\n\n0\n", () -> new ExportMenu(new Scanner(System.in)).show());
        pause("Đã chạy menu Export");

        // 9. Chạy menu khoản vay gốc với ContactMenu (đã thực hiện ở trên) + show menu chung
        runWithInput("0\n", () -> new MenuLoan().showMenu());
        pause("Đã chạy MenuLoan (thoát ngay)");

        System.out.println("=== KẾT THÚC TEST FULL CHỨC NĂNG ===");
        pause("TestScript hoàn tất");
    }

    private static void runWithInput(String text, Runnable action) {
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
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + name))
                .getId();
    }

    private static void pause(String message) throws InterruptedException {
        System.out.println(message);
        Thread.sleep(PAUSE_MS);
    }
}
