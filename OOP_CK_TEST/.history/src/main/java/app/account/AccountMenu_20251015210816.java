package app.account;

import app.ui.*;
import java.math.BigDecimal;
import java.util.Scanner;


public class AccountMenu {


    public void showMenu(Scanner sc, FinanceManager fm) {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("TÀI KHOẢN");

            System.out.println();
            System.out.println("1) Thêm tài khoản");
            System.out.println("2) Đổi tên tài khoản");
            System.out.println("3) Xóa tài khoản (chỉ khi số dư = 0)");
            System.out.println("4) Hiển thị danh sách tài khoản");
            System.out.println("0) Quay lại");
            System.out.print("Chọn (0-4): ");

            int c = readInt(sc);

            try {
                switch (c) {
                    case 1: {
                        add(sc, fm);
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case 2: {
                        rename(sc, fm);
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case 3: {
                        delete(sc, fm);
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case 4: {
                        ConsoleUtils.clear();
                        ConsoleUtils.printHeader("DANH SÁCH TÀI KHOẢN");
                        listAccounts(fm);
                        System.out.print("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case 0:
                        return;
                    default:
                        System.out.println("Giá trị không hợp lệ. Vui lòng nhập số từ 0 đến 4.");
                        ConsoleUtils.pause(sc);
                }
            } catch (Exception e) {
                System.out.println("Lỗi: " + e.getMessage());
                ConsoleUtils.pause(sc);
            }
        }
    }

    // ===== Actions =====

    private void add(Scanner sc, FinanceManager fm) {
        System.out.println("Chọn loại: 1-Ngân hàng, 2-Ví điện tử");
        int type = readIntInRange(sc, "Loại: ", 1, 2);
        String name = ConsoleUtils.readLine(sc, "Tên tài khoản: ");

        String id = (type == 1)
                ? fm.addAccount(new BankAccount(name))
                : fm.addAccount(new EWalletAccount(name));

        // Hỏi có nạp số dư ban đầu không (tùy chọn)
        if (ConsoleUtils.confirm(sc, "Bạn có muốn nạp số dư ban đầu? (y/n): ")) {
            BigDecimal amt = readAmount(sc, "Số tiền ban đầu (VND): ");
            fm.addIncome(id, amt, null, "Số dư ban đầu");
            System.out.println("Đã nạp số dư ban đầu.");
        }

        System.out.println("Đã thêm tài khoản.");
    }

    private void rename(Scanner sc, FinanceManager fm) {
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

    private void delete(Scanner sc, FinanceManager fm) {
        if (fm.listAccounts().isEmpty()) {
            System.out.println("Chưa có tài khoản nào. Vào menu 'Thêm tài khoản' để tạo mới.");
            return;
        }
        String id = ConsoleUtils.readLine(sc, "ID tài khoản cần xóa: ").trim();
        fm.deleteAccount(id);
        System.out.println("Đã xóa tài khoản.");
    }

    public void listAccounts(FinanceManager fm) {
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

    // ===== Helpers =====

    private int readInt(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                System.out.print("Vui lòng nhập số: ");
            }
        }
    }

    /** Đọc số nguyên trong khoảng [min, max]. */
    private int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int n = Integer.parseInt(s);
                if (n >= min && n <= max) return n;
            } catch (NumberFormatException ignored) {}
            System.out.printf("Giá trị không hợp lệ. Vui lòng nhập số từ %d đến %d.%n", min, max);
        }
    }
    private BigDecimal readAmount(Scanner sc, String prompt) {
        System.out.println("(Chỉ nhập số nguyên > 0; KHÔNG dùng dấu chấm/phẩy/khoảng trắng)");
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (s.isEmpty()) { System.out.println("Không được để trống."); continue; }

            // chỉ chấp nhận toàn chữ số
            boolean allDigits = true;
            for (int i = 0; i < s.length(); i++) {
                if (!Character.isDigit(s.charAt(i))) { allDigits = false; break; }
            }
            if (!allDigits) { System.out.println("Chỉ nhập số (0–9)."); continue; }

            try {
                BigDecimal v = new BigDecimal(s);
                if (v.signum() > 0) return v;
                System.out.println("Số tiền phải > 0.");
            } catch (NumberFormatException e) {
                System.out.println("Số tiền không hợp lệ.");
            }
        }
    }
    
}
