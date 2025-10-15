import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class App {
    private final TransactionService service;
    public App(TransactionService service) { this.service = service; }

    // Tạo sẵn 2 tài khoản
    public void seed() {
        service.addAccount("Ví", new BigDecimal("500000"));
        service.addAccount("Ngân hàng VCB", new BigDecimal("2000000"));
    }

    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Quản Lý Giao Dịch");
        while (true) {
            System.out.println("1) Xem số dư");
            System.out.println("2) Thêm giao dịch");
            System.out.println("3) Xem lịch sử giao dịch");
            System.out.println("4) Sửa giao dịch");
            System.out.println("5) Xóa giao dịch");
            System.out.println("q) Thoát");
            System.out.print("> ");
            String cmd = sc.nextLine().trim();
            try {
                switch (cmd) {
                    case "1": showBalances(); break;
                    case "2": addTxCLI(sc); break;
                    case "3": listTxCLI(sc); break;
                    case "4": editTxCLI(sc); break;
                    case "5": deleteTxCLI(sc); break;
                    case "q": case "Q": System.out.println("Bye!"); return;
                    default: System.out.println("Lệnh không hợp lệ.");
                }
            } catch (Exception e) { System.out.println("!! Lỗi: " + e.getMessage()); }
        }
    }

    private void showBalances() {
        System.out.println("-- SỐ DƯ --");
        BigDecimal total = BigDecimal.ZERO;
        for (Account a : service.getAccounts()) {
            BigDecimal bal = service.getBalance(a.getName());
            System.out.println("• " + a.getName() + " | Số dư: " + bal.toPlainString());
            total = total.add(bal);
        }
        System.out.println("TỔNG: " + total.toPlainString());
    }

    private String chooseAccount(Scanner sc) {
        List<Account> all = service.getAccounts();
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ") " + all.get(i).getName());
        }
        System.out.print("Chọn STT tài khoản: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= all.size()) throw new IllegalArgumentException("STT không hợp lệ");
        return all.get(idx).getName();
    }


    private void addTxCLI(Scanner sc) {
        String acc = chooseAccount(sc);
        System.out.print("Loại (1=Thu, 2=Chi): ");
        String t = sc.nextLine().trim();
        TxType type = "1".equals(t) ? TxType.INCOME : TxType.EXPENSE;
        System.out.print("Số tiền (VND): ");
        BigDecimal amount = new BigDecimal(sc.nextLine().trim().replace(',', '.'));
        LocalDate date = readDate(sc);
        System.out.print("Danh mục (bỏ trống nếu không): ");
        String cat = sc.nextLine().trim();
        System.out.print("Ghi chú: ");
        String note = sc.nextLine().trim();
        service.addTransaction(acc, type, amount, date, cat, note);
        System.out.println(">> Đã thêm. Số dư mới: " + service.getBalance(acc).toPlainString());

    }

    private void listTxCLI(Scanner sc) {
        String acc = chooseAccount(sc);
        List<Transaction> list = service.historySorted(acc);
        System.out.println("-- Lịch sử: " + acc + " --");
        if (list.isEmpty()) System.out.println("(trống)");
        else for (int i=0;i<list.size();i++) System.out.println((i+1) + ") " + list.get(i));
        System.out.println("Số dư hiện tại: " + service.getBalance(acc).toPlainString());

    }

    private void editTxCLI(Scanner sc) {
        String acc = chooseAccount(sc);
        List<Transaction> list = service.historySorted(acc);
        if (list.isEmpty()) { System.out.println("(chưa có giao dịch)"); return; }
        for (int i=0;i<list.size();i++) System.out.println((i+1) + ") " + list.get(i));
        System.out.print("Chọn STT để sửa: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        System.out.print("Loại mới (1=Thu, 2=Chi, Enter=giữ): ");
        String t = sc.nextLine().trim();
        TxType newType = t.isEmpty() ? list.get(idx).type : ("1".equals(t) ? TxType.INCOME : TxType.EXPENSE);

        System.out.print("Số tiền mới (Enter=giữ): ");
        String raw = sc.nextLine().trim();
        BigDecimal newAmount = raw.isEmpty() ? list.get(idx).amount : new BigDecimal(raw.replace(',', '.'));

        System.out.print("Ngày mới (yyyy-MM-dd, Enter=giữ): ");
        String sDate = sc.nextLine().trim();
        LocalDate newDate = sDate.isEmpty() ? list.get(idx).date : parseDate(sDate);

        System.out.print("Danh mục mới (Enter=giữ / trống=xoá): ");
        String cat = sc.nextLine().trim();
        String newCat = cat.isEmpty() ? "" : cat;

        System.out.print("Ghi chú mới (Enter=giữ): ");
        String note = sc.nextLine().trim();
        String newNote = note.isEmpty() ? list.get(idx).note : note;

        service.editTransaction(acc, idx, newType, newAmount, newDate, newCat, newNote);
        System.out.println(">> Đã cập nhật.");
    }

    private void deleteTxCLI(Scanner sc) {
        String acc = chooseAccount(sc);
        List<Transaction> list = service.historySorted(acc);
        if (list.isEmpty()) { System.out.println("(chưa có giao dịch)"); return; }
        for (int i=0;i<list.size();i++) System.out.println((i+1) + ") " + list.get(i));
        System.out.print("Chọn STT để xóa: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        service.deleteTransaction(acc, idx);
        System.out.println(">> Đã xóa. Số dư mới: " + service.getBalance(acc).toPlainString());

    }
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static LocalDate readDate(Scanner sc) {
        System.out.print("Ngày (dd/MM/yyyy, Enter=hôm nay): ");
        String s = sc.nextLine().trim();
        return s.isEmpty() ? LocalDate.now() : parseDate(s);
    }

    private static LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, DMY);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Định dạng ngày sai (đúng: dd/MM/yyyy)");
        }
    }
}
