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

    
    // Thực hiện quy trình chuyển khoản
    public static void doTransfer(Scanner sc, FinanceManager fm) {
        // (Phòng hờ) Nếu số tài khoản < 2, báo và quay lại
        if (fm.listAccounts().size() < 2) {
            System.out.println("(Cần ít nhất 2 tài khoản để chuyển khoản.)");
            System.out.print("\nNhấn Enter để quay lại...");
            sc.nextLine();
            return;
        }

        // Liệt kê tài khoản hiện có
        fm.listAccounts().forEach(a ->
                System.out.printf("• %s | ID=%s | Số dư=%s VND%n",
                        a.getName(), a.getId(), a.getBalance().toPlainString())
        );

        // Nhập ID nguồn/đích với kiểm tra + cho phép thoát
        String src = readAccountIdOrCancel(sc, fm, "ID tài khoản nguồn: ", "nguồn");
        if (src == null) { System.out.println("Đã hủy thao tác."); return; }

        String dst = readAccountIdOrCancel(sc, fm, "ID tài khoản đích: ", "đích");
        if (dst == null) { System.out.println("Đã hủy thao tác."); return; }

        if (src.equals(dst)) {
            System.out.println("Lỗi: Tài khoản nguồn và đích phải khác nhau.");
            return;
        }

        // Lấy số dư tài khoản nguồn để giới hạn số tiền
        BigDecimal srcBalance = getBalanceById(fm, src);
        System.out.println("Số dư nguồn hiện tại: " + srcBalance.toPlainString() + " VND");

        // YÊU CẦU NHẬP LẠI NẾU > SỐ DƯ
        BigDecimal amt = readAmountMax(sc, "Số tiền (VND): ", srcBalance);

        System.out.print("Ghi chú (có thể để trống): ");
        String note = sc.nextLine();

        try {
            fm.transfer(src, dst, amt, null, note);
            System.out.println("Đã chuyển khoản.");
        } catch (IllegalStateException e) {
            // Ví dụ: nguồn vừa thay đổi số dư ở nơi khác
            System.out.println("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
        }
    }

    // ===== Helpers =====
    public static boolean hasAccount(FinanceManager fm, String id) {
        return fm.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
    }

    /** Lấy số dư của tài khoản theo ID (giả định ID hợp lệ). */
    public static BigDecimal getBalanceById(FinanceManager fm, String id) {
        return fm.listAccounts().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .map(a -> a.getBalance())
                .orElse(BigDecimal.ZERO);
    }

    /** Đọc ID tài khoản; nếu không tồn tại thì hỏi nhập lại (y) hoặc thoát (n). Trả về null nếu thoát. */
    public static String readAccountIdOrCancel(Scanner sc, FinanceManager fm, String prompt, String role) {
        while (true) {
            System.out.print(prompt);
            String id = sc.nextLine().trim();
            if (hasAccount(fm, id)) return id;
            while (true) {
                System.out.print("Không tìm thấy tài khoản " + role + ": " + id +
                        ". Nhập lại? (y = tiếp tục, n = thoát): ");
                String ans = sc.nextLine().trim().toLowerCase();

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

    /** Đọc BigDecimal > 0; nếu > max thì báo “Số dư không đủ” và yêu cầu nhập lại. */
    protected static BigDecimal readAmountMax(Scanner sc, String prompt, BigDecimal max) {
        while (true) {
            BigDecimal v = Account.readAmount(sc, prompt);
            if (v.compareTo(max) <= 0) return v;
            System.out.println("Số dư không đủ");
        }
    }
}
