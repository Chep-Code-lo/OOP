package app.service;

import app.model.BankAccount;
import app.model.EWalletAccount;
import app.util.ConsoleMoneyReader;
import app.util.ConsoleUtils;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Scanner;

/** Gói các hành động thao tác với tài khoản dựa trên FinanceManager. */
public class AccountActions {
    private final FinanceManager financeManager;
    private final Scanner scanner;
    private final ConsoleMoneyReader moneyReader;

    public AccountActions(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.moneyReader = new ConsoleMoneyReader(this.scanner);
    }

    /** Thêm tài khoản mới và hỏi nạp số dư ban đầu nếu cần. */
    public void addAccount() {
        System.out.println("Chọn loại: 1-Ngân hàng, 2-Ví điện tử");
        int type = ConsoleUtils.readIntInRange(scanner, "Loại: ", 1, 2);
        String name = ConsoleUtils.readLine(scanner, "Tên tài khoản: ").trim();

        String id = (type == 1)
                ? financeManager.addAccount(new BankAccount(name))
                : financeManager.addAccount(new EWalletAccount(name));

        if (ConsoleUtils.confirm(scanner, "Bạn có muốn nạp số dư ban đầu?")) {
            BigDecimal amt = moneyReader.readAmount("Số tiền ban đầu (VND): ");
            financeManager.addIncome(id, amt, null, "Số dư ban đầu");
            System.out.println("Đã nạp số dư ban đầu.");
        }

        System.out.println("Đã thêm tài khoản.");
    }

    /** Đổi tên tài khoản theo ID. */
    public void renameAccount() {
        if (financeManager.listAccounts().isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }

        String id = ConsoleUtils.readLine(scanner, "ID tài khoản cần đổi tên: ").trim();
        boolean exists = financeManager.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
        if (!exists) {
            System.out.println("Lỗi: Không tìm thấy tài khoản: " + id);
            return;
        }

        String newName = ConsoleUtils.readLine(scanner, "Tên mới: ").trim();
        financeManager.updateAccountName(id, newName);
        System.out.println("Đã đổi tên tài khoản.");
    }

    /** Xoá tài khoản nếu hợp lệ. */
    public void deleteAccount() {
        if (financeManager.listAccounts().isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }

        String id = ConsoleUtils.readLine(scanner, "ID tài khoản cần xóa: ").trim();
        financeManager.deleteAccount(id);
        System.out.println("Đã xóa tài khoản.");
    }

    /** Liệt kê tất cả tài khoản hiện có. */
    public void listAccounts() {
        var list = financeManager.listAccounts();
        if (list.isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }
        for (var a : list) {
            System.out.printf("- %s | %s | ID: %s | VNĐ | số dư: %s%n",
                    a.getName(), a.getType(), a.getId(), a.getBalance());
        }
    }

    /** Thực hiện quy trình chuyển tiền giữa hai tài khoản. */
    public void transferBetweenAccounts() {
        if (financeManager.listAccounts().size() < 2) {
            System.out.println("(Cần ít nhất 2 tài khoản để chuyển khoản.)");
            System.out.print("\nNhấn Enter để quay lại...");
            scanner.nextLine();
            return;
        }

        financeManager.listAccounts().forEach(a ->
                System.out.printf("• %s | ID=%s | Số dư=%s VND%n",
                        a.getName(), a.getId(), a.getBalance().toPlainString())
        );

        String src = readAccountIdOrCancel("ID tài khoản nguồn: ", "nguồn");
        if (src == null) {
            System.out.println("Đã hủy thao tác.");
            return;
        }

        String dst = readAccountIdOrCancel("ID tài khoản đích: ", "đích");
        if (dst == null) {
            System.out.println("Đã hủy thao tác.");
            return;
        }

        if (src.equals(dst)) {
            System.out.println("Lỗi: Tài khoản nguồn và đích phải khác nhau.");
            return;
        }

        BigDecimal srcBalance = getBalanceById(src);
        System.out.println("Số dư nguồn hiện tại: " + srcBalance.toPlainString() + " VND");

        BigDecimal amount = readAmountAtMost(srcBalance, "Số tiền (VND): ");

        System.out.print("Ghi chú (có thể để trống): ");
        String note = scanner.nextLine();

        try {
            financeManager.transfer(src, dst, amount, null, note);
            System.out.println("Đã chuyển khoản.");
        } catch (IllegalStateException e) {
            System.out.println("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    private boolean hasAccount(String id) {
        return financeManager.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
    }

    private BigDecimal getBalanceById(String id) {
        return financeManager.listAccounts().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .map(a -> a.getBalance())
                .orElse(BigDecimal.ZERO);
    }

    private String readAccountIdOrCancel(String prompt, String role) {
        while (true) {
            System.out.print(prompt);
            String id = scanner.nextLine().trim();
            if (hasAccount(id)) {
                return id;
            }

            while (true) {
                System.out.print("Không tìm thấy tài khoản " + role + ": " + id +
                        ". Nhập lại? (y = tiếp tục, n = thoát): ");
                String ans = scanner.nextLine().trim().toLowerCase();
                if (ans.equals("n") || ans.equals("no")) {
                    return null;
                }
                if (ans.equals("y") || ans.equals("yes")) {
                    break;
                }
                System.out.println("Vui lòng nhập (y/n).");
            }
        }
    }

    private BigDecimal readAmountAtMost(BigDecimal max, String prompt) {
        while (true) {
            BigDecimal value = moneyReader.readAmount(prompt);
            if (value.compareTo(max) <= 0) {
                return value;
            }
            System.out.println("Số dư không đủ");
        }
    }
}
