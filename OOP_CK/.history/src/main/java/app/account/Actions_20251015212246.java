package app.account;

import java.math.BigDecimal;
import java.util.Scanner;

import app.ui.ConsoleUtils;

public class Actions {
     public static void add(Scanner sc, FinanceManager fm) {
        System.out.println("Chọn loại: 1-Ngân hàng, 2-Ví điện tử");
        int type = ConsoleUtils.readIntInRange(sc, "Loại: ", 1, 2);
        String name = ConsoleUtils.readLine(sc, "Tên tài khoản: ");

        String id = (type == 1)
                ? fm.addAccount(new BankAccount(name))
                : fm.addAccount(new EWalletAccount(name));

        // Hỏi có nạp số dư ban đầu không (tùy chọn)
        if (ConsoleUtils.confirm(sc, "Bạn có muốn nạp số dư ban đầu? (y/n): ")) {
            BigDecimal amt = Account.readAmount(sc, "Số tiền ban đầu (VND): ");
            fm.addIncome(id, amt, null, "Số dư ban đầu");
            System.out.println("Đã nạp số dư ban đầu.");
        }

        System.out.println("Đã thêm tài khoản.");
    }

    public static void rename(Scanner sc, FinanceManager fm) {
        if (fm.listAccounts().isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }

        String id = ConsoleUtils.readLine(sc, "ID tài khoản cần đổi tên: ").trim();

        boolean exists = fm.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
        if (!exists) {
            System.out.println("Lỗi: Không tìm thấy tài khoản: " + id);
            return; // không hỏi tên mới nữa
        }

        String newName = ConsoleUtils.readLine(sc, "Tên mới: ").trim();
        fm.updateAccountName(id, newName);
        System.out.println("Đã đổi tên tài khoản.");
    }

    public static void delete(Scanner sc, FinanceManager fm) {
        if (fm.listAccounts().isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }
        String id = ConsoleUtils.readLine(sc, "ID tài khoản cần xóa: ").trim();
        fm.deleteAccount(id);
        System.out.println("Đã xóa tài khoản.");
    }

    public static void listAccounts(FinanceManager fm) {
        var list = fm.listAccounts();
        if (list.isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }
        for (var a : list) {
            System.out.printf("- %s | %s | ID: %s | VNĐ | số dư: %s%n",
                    a.getName(), a.getType(), a.getId(), a.getBalance());
        }
    }
}
