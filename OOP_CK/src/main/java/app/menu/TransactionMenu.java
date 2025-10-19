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

public class TransactionMenu {
    private final FinanceManager financeManager;
    private final TransactionService transactionService;
    private final Scanner scanner;
    private final ConsoleMoneyReader moneyReader;

    public TransactionMenu(FinanceManager financeManager, Scanner scanner) {
        this.financeManager = Objects.requireNonNull(financeManager, "financeManager");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.transactionService = new TransactionService(this.financeManager);
        this.moneyReader = new ConsoleMoneyReader(this.scanner);
    }

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

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private String formatDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneId.systemDefault()).toString();
    }
}
