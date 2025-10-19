package app;

import app.App;
import app.account.AccountMenu;
import app.account.BankAccount;
import app.account.FinanceManager;
import app.account.TransferMenu;
import app.export.ExportAccounts;
import app.export.ExportLoans;
import app.export.ExportTransactions;
import app.loan.ContractStorage;
import app.store.DataStore;
import app.transaction.TransactionService;
import app.ui.ExportMenu;
import app.ui.MenuLoan;
import app.ui.Menutransaction;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Tập hợp các bước kiểm thử end-to-end cho toàn bộ menu của ứng dụng.
 * Bao gồm cả các trường hợp đặc biệt như thao tác lỗi, dữ liệu trùng, xóa khi còn số dư...
 */
public final class TestScript {
    private static final long DELAY_MS = 250L;
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path ACCOUNTS_CSV = DATA_DIR.resolve("accounts.csv");
    private static final Path CONTRACTS_CSV = DATA_DIR.resolve("contracts.csv");
    private static final Path LOANS_CSV = DATA_DIR.resolve("loans.csv");
    private static final Path TRANSACTIONS_CSV = DATA_DIR.resolve("transactions.csv");
    private static final Path FAILURE_LOG = DATA_DIR.resolve("test_failures.txt");
    private static final Path PASS_LOG = DATA_DIR.resolve("test_passes.txt");

    private TestScript() {}

    public static void main(String[] args) throws Exception {
        List<String> failed = new ArrayList<>();
        List<String> passed = new ArrayList<>();

        runStep("Dọn dữ liệu cũ", passed, failed, TestScript::cleanupEnvironment);

        FinanceManager fm = new FinanceManager();
        TransactionService txService = new TransactionService(fm);

        final String[] bankId = new String[1];
        final String[] walletId = new String[1];

        runStep("Tạo hai tài khoản qua menu", passed, failed, () -> {
            runWithInput("""
                    1
                    1
                    BankDemo
                    n

                    1
                    2
                    WalletDemo
                    n

                    0
                    """, () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
            bankId[0] = findAccountIdByName(fm, "BankDemo");
            walletId[0] = findAccountIdByName(fm, "WalletDemo");
            ensure(bankId[0] != null && walletId[0] != null, "Chưa khởi tạo đủ tài khoản");
        });

        runStep("Không cho phép trùng tên tài khoản", passed, failed, () ->
                expectThrows(IllegalArgumentException.class,
                        () -> fm.addAccount(new BankAccount("BankDemo"))));

        final String renamedBank = "BankMaster";
        runStep("Đổi tên tài khoản BankDemo qua menu", passed, failed, () -> {
            runWithInput(String.format("""
                    2
                    %s
                    %s

                    0
                    """, bankId[0], renamedBank), () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
            ensure(Objects.equals(fm.requireAccount(bankId[0]).getName(), renamedBank),
                    "Tên tài khoản không được cập nhật");
            ensure(fileContent(ACCOUNTS_CSV).contains(renamedBank), "accounts.csv chưa cập nhật tên mới");
        });

        runStep("Xem danh sách tài khoản qua menu (để đảm bảo luồng 4)", passed, failed, () ->
                runWithInput("""
                        4

                        0
                        """, () -> new AccountMenu(fm, new Scanner(System.in)).showMenu()));

        runStep("Chuyển khoản 1000 từ Bank sang Wallet qua menu", passed, failed, () ->
                runWithInput(String.format("""
                        1
                        %s
                        %s
                        1000
                        Chuyen thu 1000
                        2
                        """, bankId[0], walletId[0]), () -> new TransferMenu(fm, new Scanner(System.in)).showMenu()));

        runStep("Nạp thêm thu vào Bank và Wallet", passed, failed, () -> {
            fm.addIncome(bankId[0], new BigDecimal("4000"), Instant.now(), "Seed bank");
            fm.addIncome(walletId[0], new BigDecimal("1500"), Instant.now(), "Seed wallet");
        });

        runStep("Không thể xóa tài khoản khi còn số dư", passed, failed, () ->
                expectThrows(IllegalStateException.class, () -> fm.deleteAccount(walletId[0])));

        runStep("Thêm giao dịch chi 250 cho Wallet qua menu giao dịch", passed, failed, () -> {
            runWithInput("""
                    2
                    2
                    2
                    250
                    01/01/2025
                    An uong
                    Chi demo

                    0
                    """, () -> new Menutransaction(fm, new Scanner(System.in)).showMenu());
            ensure(txService.historySorted(walletId[0]).size() == 1,
                    "Không ghi nhận giao dịch chi");
        });

        runStep("Sửa giao dịch vừa tạo qua menu (đổi số tiền thành 200)", passed, failed, () -> {
            runWithInput("""
                    4
                    2
                    1

                    200
                    02/01/2025
                    Sinh hoat
                    Note moi

                    0
                    """, () -> new Menutransaction(fm, new Scanner(System.in)).showMenu());
            var history = txService.historySorted(walletId[0]);
            ensure(history.size() == 1 && history.get(0).getAmount().compareTo(new BigDecimal("200.00")) == 0,
                    "Không cập nhật được giao dịch");
        });

        runStep("Xem lịch sử giao dịch qua menu", passed, failed, () ->
                runWithInput("""
                        3
                        2

                        0
                        """, () -> new Menutransaction(fm, new Scanner(System.in)).showMenu()));

        runStep("Xóa giao dịch qua menu", passed, failed, () -> {
            runWithInput("""
                    5
                    2
                    1

                    0
                    """, () -> new Menutransaction(fm, new Scanner(System.in)).showMenu());
            ensure(txService.historySorted(walletId[0]).isEmpty(), "Giao dịch chưa bị xóa");
        });

        runStep("Không sửa được giao dịch không tồn tại", passed, failed, () ->
                expectThrows(IllegalArgumentException.class, () ->
                        txService.editTransaction(walletId[0], "INVALID",
                                null, null, null, null, null)));

        runStep("Tạo hợp đồng lãi đơn qua menu Loan", passed, failed, () ->
                runWithInput("""
                        1
                        1
                        Nguoi vay A
                        5000
                        0911222333
                        01/01/2025
                        01/06/2025
                        10
                        1
                        Ghi chu lai don
                        3

                        0
                        """, () -> new MenuLoan().showMenu()));

        runStep("Tạo hợp đồng không lãi qua menu Loan", passed, failed, () ->
                runWithInput("""
                        1
                        2
                        Nguoi vay B
                        3000
                        0987654321
                        05/02/2025
                        05/03/2025
                        0
                        Ghi chu khong lai
                        3

                        0
                        """, () -> new MenuLoan().showMenu()));

        runStep("Tính lãi cho cả hai hợp đồng", passed, failed, () ->
                runWithInput("""
                        5
                        1

                        5
                        2

                        0
                        """, () -> new MenuLoan().showMenu()));

        runStep("Xem số ngày đến hạn của hợp đồng đầu tiên", passed, failed, () ->
                runWithInput("""
                        4
                        1

                        0
                        """, () -> new MenuLoan().showMenu()));

        runStep("Cập nhật hợp đồng đầu tiên", passed, failed, () ->
                runWithInput("""
                        2
                        1
                        Nguoi vay A cap nhat


                        01/07/2025
                        6000
                        12
                        COMPOUND
                        Cap nhat note

                        0
                        """, () -> new MenuLoan().showMenu()));

        runStep("Xóa lần lượt các hợp đồng qua menu", passed, failed, () ->
                runWithInput("""
                        3
                        1
                        y

                        3
                        1
                        y

                        0
                        """, () -> new MenuLoan().showMenu()));

        runStep("Đảm bảo đã xóa sạch hợp đồng trong bộ nhớ và CSV", passed, failed, () -> {
            ensure(ContractStorage.loadAll().isEmpty(), "Vẫn còn hợp đồng trong ContractStorage");
            ensure(fileContent(CONTRACTS_CSV).lines().count() == 1, "contracts.csv chưa được cập nhật");
        });

        runStep("Chuyển toàn bộ số dư ví về Bank và xóa ví qua menu", passed, failed, () -> {
            BigDecimal walletBalance = fm.requireAccount(walletId[0]).getBalance();
            if (walletBalance.signum() > 0) {
                fm.transfer(walletId[0], bankId[0], walletBalance, Instant.now(), "Tất toán ví");
            }
            runWithInput(String.format("""
                    3
                    %s

                    0
                    """, walletId[0]), () -> new AccountMenu(fm, new Scanner(System.in)).showMenu());
            ensure(fm.listAccounts().stream().noneMatch(a -> Objects.equals(a.getId(), walletId[0])),
                    "Ví vẫn còn tồn tại sau khi xóa");
            ensure(!fileContent(ACCOUNTS_CSV).contains("WalletDemo"),
                    "accounts.csv chưa xóa ví");
        });

        runStep("Xuất CSV cho tất cả dữ liệu", passed, failed, () -> {
            ExportAccounts.export();
            ExportTransactions.export();
            ExportLoans.export();
            runWithInput("""
                    5

                    0
                    """, () -> new ExportMenu(new Scanner(System.in)).show());
        });

        runStep("Kiểm tra file CSV tồn tại và có dữ liệu", passed, failed, () -> {
            ensure(Files.exists(ACCOUNTS_CSV), "Thiếu accounts.csv");
            ensure(Files.exists(CONTRACTS_CSV), "Thiếu contracts.csv");
            ensure(Files.exists(LOANS_CSV), "Thiếu loans.csv");
        });

        runStep("Chạy App chính với lựa chọn sai để kiểm tra lỗi nhập", passed, failed, () ->
                runWithInput("""
                        9

                        0
                        """, () -> App.main(new String[0])));

        Files.createDirectories(DATA_DIR);
        Files.write(PASS_LOG, passed);

        if (failed.isEmpty()) {
            Files.deleteIfExists(FAILURE_LOG);
            System.out.println("\n=== KẾT THÚC: TOÀN BỘ KIỂM THỬ THÀNH CÔNG ===");
        } else {
            Files.write(FAILURE_LOG, failed);
            System.out.println("\n=== KẾT THÚC: " + failed.size() + " BƯỚC THẤT BẠI ===");
            System.out.println("Chi tiết lỗi đã được ghi vào " + FAILURE_LOG.toAbsolutePath());
            failed.forEach(name -> System.out.println(" - " + name));
            throw new IllegalStateException("Một hoặc nhiều bước kiểm thử thất bại");
        }
    }

    private static void runStep(String name, List<String> passed, List<String> failed, CheckedRunnable action) {
        System.out.println("\n--- " + name + " ---");
        try {
            action.run();
            System.out.println("[PASS] " + name);
            passed.add(name);
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
            System.out.println("[INPUT]\n" + text);
            System.setIn(new ByteArrayInputStream(text.getBytes()));
            Thread.sleep(DELAY_MS);
            action.run();
        } finally {
            System.setIn(original);
        }
    }

    private static void cleanupEnvironment() throws IOException {
        Files.createDirectories(DATA_DIR);
        Files.deleteIfExists(ACCOUNTS_CSV);
        Files.deleteIfExists(CONTRACTS_CSV);
        Files.deleteIfExists(LOANS_CSV);
        Files.deleteIfExists(TRANSACTIONS_CSV);
        DataStore.clearAll();
    }

    private static String findAccountIdByName(FinanceManager fm, String name) {
        return fm.listAccounts().stream()
                .filter(a -> Objects.equals(a.getName(), name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + name))
                .getId();
    }

    private static void ensure(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static String fileContent(Path path) throws IOException {
        return Files.exists(path) ? Files.readString(path) : "";
    }

    private static void expectThrows(Class<? extends Throwable> type, CheckedRunnable action) throws Exception {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) return;
            throw new IllegalStateException("Mong đợi " + type.getSimpleName() + " nhưng nhận " + t.getClass().getSimpleName(), t);
        }
        throw new IllegalStateException("Mong đợi ngoại lệ " + type.getSimpleName() + " nhưng không có");
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}
