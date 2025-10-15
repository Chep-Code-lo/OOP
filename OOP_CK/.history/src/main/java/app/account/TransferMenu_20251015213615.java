package app.account;

import app.ui.*;
import java.math.BigDecimal;
import java.util.Scanner;

/** Menu 2: Chuyển khoản giữa các tài khoản - format showMenu(Scanner, FinanceManager). */
public class TransferMenu {

    public void showMenu(Scanner sc, FinanceManager fm) {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("CHUYỂN KHOẢN");

            // cần tối thiểu 2 tài khoản ====
            int count = fm.listAccounts().size();
            if (count < 2) {
                if (count == 0) {
                    System.out.println("(Chưa có tài khoản nào. Vào menu 1 để tạo tài khoản trước.)");
                } else {
                    System.out.println("(Chỉ có 1 tài khoản. Cần ít nhất 2 tài khoản để chuyển khoản.)");
                }
                System.out.print("\nNhấn Enter để quay lại...");
                sc.nextLine();
                return;
            }

            System.out.println("1) Thực hiện chuyển khoản");
            System.out.println("2) Quay lại menu chính");
            System.out.print("Bạn muốn: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1"->{
                    doTransfer(sc, fm);
                    break;
                }
                case "2"->{
                    return;
                }
                default->{
                    System.out.println("Lựa chọn không hợp lệ. Nhấn Enter để thử lại...");
                    sc.nextLine();
                }
            }
        }
    }

    // Thực hiện quy trình chuyển khoản
    private void doTransfer(Scanner sc, FinanceManager fm) {
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
    private boolean hasAccount(FinanceManager fm, String id) {
        return fm.listAccounts().stream().anyMatch(a -> a.getId().equals(id));
    }

    /** Lấy số dư của tài khoản theo ID (giả định ID hợp lệ). */
    private BigDecimal getBalanceById(FinanceManager fm, String id) {
        return fm.listAccounts().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .map(a -> a.getBalance())
                .orElse(BigDecimal.ZERO);
    }

    /** Đọc ID tài khoản; nếu không tồn tại thì hỏi nhập lại (y) hoặc thoát (n). Trả về null nếu thoát. */
    private String readAccountIdOrCancel(Scanner sc, FinanceManager fm, String prompt, String role) {
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
    private BigDecimal readAmountMax(Scanner sc, String prompt, BigDecimal max) {
        while (true) {
            BigDecimal v = Account.readAmount(sc, prompt);
            if (v.compareTo(max) <= 0) return v;
            System.out.println("Số dư không đủ");
        }
    }

}
