package app.menu;

import app.model.Account;
import app.model.Transaction;
import app.model.TxnType;
import app.service.FinanceManager;
import app.service.TransactionService;
import app.util.ConsoleMoneyReader;
import app.util.ConsoleUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/** Menu thao tác giao dịch thu/chi, cung cấp các bước nhập liệu cho người dùng. */
public class TransactionMenu {
    private final FinanceManager financeManager;
    private final TransactionService transactionService;
    private final Scanner scanner;
    private final ConsoleMoneyReader moneyReader;

    /** Khởi tạo menu với lớp nghiệp vụ và tiện ích đọc tiền. */
    public TransactionMenu(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.transactionService = new TransactionService(this.financeManager);
        this.moneyReader = new ConsoleMoneyReader(this.scanner);
    }

    /** Vòng lặp hiển thị menu giao dịch và điều phối thao tác theo lựa chọn người dùng. */
    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("QUẢN LÝ GIAO DỊCH");
            System.out.println("1) Xem số dư");
            System.out.println("2) Thêm giao dịch");
            System.out.println("3) Xem lịch sử giao dịch");
            System.out.println("4) Sửa giao dịch");
            System.out.println("5) Xóa giao dịch");
            System.out.println("0) Thoát");
            System.out.print("Bạn muốn: ");

            try {
                switch (scanner.nextLine().trim()) {
                    case "1" -> {
                        showBalances();
                        ConsoleUtils.pause(scanner);
                    }
                    case "2" -> {
                        addTransaction();
                        ConsoleUtils.pause(scanner);
                    }
                    case "3" -> {
                        listTransactions();
                        ConsoleUtils.pause(scanner);
                    }
                    case "4" -> {
                        editTransaction();
                        ConsoleUtils.pause(scanner);
                    }
                    case "5" -> {
                        deleteTransaction();
                        ConsoleUtils.pause(scanner);
                    }
                    case "0" -> {
                        System.out.println("Tạm biệt!");
                        return;
                    }
                    default -> System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 5!");
                }
            } catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(scanner);
            }
        }
    }

    /** In số dư hiện tại của tất cả tài khoản. */
    private void showBalances() {
        ConsoleUtils.printHeader("SỐ DƯ TÀI KHOẢN");
        List<Account> accounts = new ArrayList<>(financeManager.listAccounts());
        if (accounts.isEmpty()) {
            System.out.println("(Chưa có tài khoản nào.)");
            return;
        }
        accounts.forEach(a ->
                System.out.printf("• %s | ID=%s | Số dư=%s VND%n",
                        a.getName(), a.getId(), a.getBalance().toPlainString()));
    }

    /** Thu thập dữ liệu và gọi service để ghi một giao dịch mới. */
    private void addTransaction() {
        ConsoleUtils.printHeader("THÊM GIAO DỊCH");
        String accountId = chooseAccount();
        if (accountId == null) {
            System.out.println("Đã hủy thao tác.");
            return;
        }
        TxnType type = readTxnType("Loại (1=Thu, 2=Chi): ", false);
        BigDecimal amount = moneyReader.readAmount("Số tiền (VND): ");
        LocalDate date = readRequiredDate("Ngày giao dịch (YYYY-MM-DD): ");
        System.out.print("Danh mục: ");
        String category = scanner.nextLine().trim();
        System.out.print("Ghi chú: ");
        String note = scanner.nextLine().trim();

        transactionService.addTransaction(accountId, type, amount, date, category, note);
        System.out.println("✔ Đã ghi nhận giao dịch.");
    }

    /** Liệt kê các giao dịch của một tài khoản theo thứ tự thời gian. */
    private void listTransactions() {
        ConsoleUtils.printHeader("LỊCH SỬ GIAO DỊCH");
        String accountId = chooseAccount();
        if (accountId == null) {
            System.out.println("Đã hủy thao tác.");
            return;
        }
        List<Transaction> entries = transactionService.historySorted(accountId);
        if (entries.isEmpty()) {
            System.out.println("(Chưa có giao dịch nào.)");
            return;
        }
        entries.forEach(tx -> System.out.printf("- %s | %s | %s VND | %s | %s%n",
                tx.getId(),
                tx.getType(),
                tx.getAmount().toPlainString(),
                formatDate(tx.getOccurredAt()),
                tx.getNote()));
    }

    /** Cho phép chỉnh sửa giao dịch đã chọn với các trường nhập tuỳ chọn. */
    private void editTransaction() {
        ConsoleUtils.printHeader("SỬA GIAO DỊCH");
        String accountId = chooseAccount();
        if (accountId == null) {
            System.out.println("Đã hủy thao tác.");
            return;
        }
        System.out.print("ID giao dịch cần sửa: ");
        String txnId = scanner.nextLine().trim();
        TxnType newType = readTxnType("Loại mới (1=Thu, 2=Chi, Enter = giữ nguyên): ", true);
        BigDecimal newAmount = readOptionalAmount("Số tiền mới (VND, Enter = giữ nguyên): ");
        LocalDate newDate = readOptionalDate("Ngày mới (YYYY-MM-DD, Enter = giữ nguyên): ");
        System.out.print("Danh mục mới (Enter = giữ nguyên): ");
        String newCategory = blankToNull(scanner.nextLine().trim());
        System.out.print("Ghi chú mới (Enter = giữ nguyên): ");
        String newNote = blankToNull(scanner.nextLine().trim());

        transactionService.editTransaction(accountId, txnId, newType, newAmount, newDate, newCategory, newNote);
        System.out.println("✔ Đã cập nhật giao dịch.");
    }

    /** Xoá giao dịch sau khi xác định tài khoản và mã giao dịch. */
    private void deleteTransaction() {
        ConsoleUtils.printHeader("XÓA GIAO DỊCH");
        String accountId = chooseAccount();
        if (accountId == null) {
            System.out.println("Đã hủy thao tác.");
            return;
        }
        System.out.print("ID giao dịch cần xóa: ");
        String txnId = scanner.nextLine().trim();
        transactionService.deleteTransaction(accountId, txnId);
        System.out.println("✔ Đã xóa giao dịch.");
    }

    /** Hiển thị danh sách tài khoản và trả về ID được chọn (hoặc null nếu huỷ). */
    private String chooseAccount() {
        List<Account> accounts = new ArrayList<>(financeManager.listAccounts());
        if (accounts.isEmpty()) {
            System.out.println("(Chưa có tài khoản nào. Vào menu tài khoản để thêm trước.)");
            return null;
        }
        accounts.forEach(a ->
                System.out.printf("• %s | ID=%s | Số dư=%s VND%n",
                        a.getName(), a.getId(), a.getBalance().toPlainString()));
        System.out.print("Nhập ID tài khoản (hoặc Enter để hủy): ");
        String id = scanner.nextLine().trim();
        if (id.isEmpty()) return null;
        boolean exists = accounts.stream().anyMatch(a -> a.getId().equals(id));
        if (!exists) {
            System.out.println("Không tìm thấy tài khoản: " + id);
            return null;
        }
        return id;
    }

    /** Đọc lựa chọn loại giao dịch (thu/chi); cho phép bỏ qua nếu optional=true. */
    private TxnType readTxnType(String prompt, boolean optional) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (optional && input.isEmpty()) {
                return null;
            }
            if (input.equals("1")) return TxnType.INCOME;
            if (input.equals("2")) return TxnType.EXPENSE;
            System.out.println("Chỉ chấp nhận 1 (Thu) hoặc 2 (Chi).");
        }
    }

    /** Đọc số tiền hợp lệ; trả về null nếu người dùng bỏ qua. */
    private BigDecimal readOptionalAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                BigDecimal value = new BigDecimal(input);
                if (value.signum() > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Số tiền không hợp lệ, vui lòng nhập lại.");
        }
    }

    /** Đọc ngày bắt buộc theo định dạng YYYY-MM-DD. */
    private LocalDate readRequiredDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("Ngày không hợp lệ, định dạng phải là YYYY-MM-DD.");
            }
        }
    }

    /** Đọc ngày tuỳ chọn, trả về null khi người dùng bỏ trống. */
    private LocalDate readOptionalDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return LocalDate.parse(input);
            } catch (Exception e) {
                System.out.println("Ngày không hợp lệ, định dạng phải là YYYY-MM-DD.");
            }
        }
    }

    /** Hỗ trợ chuyển chuỗi trống thành null để tiện truyền vào service. */
    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    /** Định dạng lại Instant thành chuỗi ngày (YYYY-MM-DD). */
    private String formatDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault()).toString();
    }
}
