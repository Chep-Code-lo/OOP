package app.transaction;

import app.account.*;
import app.ui.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Giao diện console cho module giao dịch thu/chi. */
public class App {
    public final TransactionService service;

    public App(TransactionService service) { this.service = service; }

    public void showBalances() {
        System.out.println("-- SỐ DƯ --");
        BigDecimal total = BigDecimal.ZERO;
        List<Account> accounts = service.getAccounts();
        if (accounts.isEmpty()) {
            System.out.println("(chưa có tài khoản)");
        } else {
            for (Account a : accounts) {
                BigDecimal bal = service.getBalance(a.getId());
                System.out.println("• " + a.getName() + " [" + a.getId() + "] | Số dư: " + bal.toPlainString());
                total = total.add(bal);
            }
        }
        System.out.println("TỔNG: " + total.toPlainString());
    }

    /** Hiển thị danh sách và cho phép người dùng chọn tài khoản bằng STT. */
    public String chooseAccount(Scanner sc) {
        List<Account> all = service.getAccounts();
        if (all.isEmpty()) throw new IllegalStateException("Chưa có tài khoản nào. Hãy tạo ở menu tài khoản.");
        for (int i = 0; i < all.size(); i++) {
            Account a = all.get(i);
            System.out.println((i + 1) + ") " + a.getName() + " [" + a.getId() + "]");
        }
        System.out.print("Chọn STT tài khoản: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= all.size()) throw new IllegalArgumentException("STT không hợp lệ");
        return all.get(idx).getId();
    }

    /** Quy trình thêm giao dịch thu/chi mới từ console. */
    public void addTxCLI(Scanner sc) {
        String accountId = chooseAccount(sc);
        System.out.print("Loại (1=Thu, 2=Chi): ");
        String t = sc.nextLine().trim();
        TxnType type = "1".equals(t) ? TxnType.INCOME : TxnType.EXPENSE;
        System.out.print("Số tiền (VND): ");
        BigDecimal amount = new BigDecimal(sc.nextLine().trim().replace(',', '.'));
        LocalDate date = DateUtils.readDate(sc);
        System.out.print("Danh mục (bỏ trống nếu không): ");
        String cat = sc.nextLine().trim();
        System.out.print("Ghi chú: ");
        String note = sc.nextLine().trim();
        service.addTransaction(accountId, type, amount, date, cat, note);
        System.out.println(">> Đã thêm. Số dư mới: " + service.getBalance(accountId).toPlainString());
    }

    /** Liệt kê lịch sử giao dịch của một tài khoản. */
    public void listTxCLI(Scanner sc) {
        String accountId = chooseAccount(sc);
        String accountName = service.resolveAccountName(accountId);
        List<Transaction> list = service.historySorted(accountId);
        System.out.println("-- Lịch sử: " + accountName + " [" + accountId + "] --");
        if (list.isEmpty()) System.out.println("(trống)");
        else for (int i = 0; i < list.size(); i++) System.out.println((i + 1) + ") " + list.get(i));
        System.out.println("Số dư hiện tại: " + service.getBalance(accountId).toPlainString());
    }

    /** Cho phép sửa một giao dịch đã ghi. */
    public void editTxCLI(Scanner sc) {
        String accountId = chooseAccount(sc);
        List<Transaction> list = service.historySorted(accountId);
        if (list.isEmpty()) { System.out.println("(chưa có giao dịch)"); return; }
        for (int i = 0; i < list.size(); i++) System.out.println((i + 1) + ") " + list.get(i));
        System.out.print("Chọn STT để sửa: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        System.out.print("Loại mới (1=Thu, 2=Chi, Enter=giữ): ");
        String t = sc.nextLine().trim();
        TxnType newType = t.isEmpty() ? null : ("1".equals(t) ? TxnType.INCOME : TxnType.EXPENSE);

        System.out.print("Số tiền mới (Enter=giữ): ");
        String raw = sc.nextLine().trim();
        BigDecimal newAmount = raw.isEmpty() ? null : new BigDecimal(raw.replace(',', '.'));

        System.out.print("Ngày mới (dd/MM/yyyy, Enter=giữ): ");
        String sDate = sc.nextLine().trim();
        LocalDate newDate = sDate.isEmpty() ? null : DateUtils.parseDate(sDate);

        System.out.print("Danh mục mới (Enter=giữ, '-'=xoá): ");
        String cat = sc.nextLine().trim();
        String newCat = cat.isEmpty() ? null : ("-".equals(cat) ? "" : cat);

        System.out.print("Ghi chú mới (Enter=giữ, '-'=xoá): ");
        String note = sc.nextLine().trim();
        String newNote = note.isEmpty() ? null : ("-".equals(note) ? "" : note);

        if (idx < 0 || idx >= list.size()) throw new IllegalArgumentException("STT không hợp lệ");
        Transaction selected = list.get(idx);
        service.editTransaction(accountId, selected.id, newType, newAmount, newDate, newCat, newNote);
        System.out.println(">> Đã cập nhật.");
    }

    /** Xóa một giao dịch và hoàn tiền về tài khoản gốc. */
    public void deleteTxCLI(Scanner sc) {
        String accountId = chooseAccount(sc);
        List<Transaction> list = service.historySorted(accountId);
        if (list.isEmpty()) { System.out.println("(chưa có giao dịch)"); return; }
        for (int i = 0; i < list.size(); i++) System.out.println((i + 1) + ") " + list.get(i));
        System.out.print("Chọn STT để xóa: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= list.size()) throw new IllegalArgumentException("STT không hợp lệ");
        Transaction selected = list.get(idx);
        service.deleteTransaction(accountId, selected.id);
        System.out.println(">> Đã xóa. Số dư mới: " + service.getBalance(accountId).toPlainString());
    }

}
