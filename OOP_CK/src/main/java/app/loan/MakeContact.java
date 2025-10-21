package app.loan;

import app.model.Account;
import app.model.Contract;
import app.repository.ContractStorage;
import app.service.FinanceManager;
import app.util.DateUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Tạo và lưu hợp đồng vay/cho vay mới. */
public class MakeContact {
    
    /** Thu thập thông tin từ console để xây dựng Contract hợp lệ. */
    public static Contract Create(FinanceManager financeManager, Scanner sc, Contract.Stats stats) {
        if (financeManager == null) throw new IllegalArgumentException("financeManager");

        Account account = selectAccount(financeManager, sc);
        if (account == null) {
            System.out.println("Huỷ tạo hợp đồng.");
            return null;
        }

        String name = ReadInfor.readNonBlank(sc, "Tên người liên hệ: ");
        String money = ReadInfor.readNonBlank(sc, "Số tiền vay/cho vay: ");

        String phone;
        while (true) {
            phone = ReadInfor.readNonBlank(sc, "Số điện thoại (10 số): ");
            if (ValidInfor.isValidPhone10(phone)) break;
            System.out.println("Số điện thoại không hợp lệ. Ví dụ: 0912345678");
        }

        String vay;
        while (true) {
            vay = ReadInfor.readNonBlank(sc,"Ngày tạo hợp đồng (DD/MM/YYYY): ");
            if (DateUtils.isValidDDMMYY(vay)) break;
            System.out.println("Ngày không hợp lệ (ví dụ 34/4/24 sẽ bị từ chối). Nhập lại!");
        }

        String due;
        while (true) {
            due = ReadInfor.readNonBlank(sc, "Hạn trả nợ (DD/MM/YYYY): ");
            if (DateUtils.isValidDDMMYY(due)) break;
            System.out.println("Ngày không hợp lệ (ví dụ 31/04/24 sẽ bị từ chối). Nhập lại!");
        }

        double interest = ReadInfor.readPositiveDouble(sc, "Lãi suất (%): ");
        
        int choice = 0;
        if (interest != 0){
            System.out.println("Hãy chọn loại lãi suất  ");
            System.out.println("1. Lãi đơn");
            System.out.println("2. Lãi kép");
            choice = CheckInfor.checkOp(sc, 1, 2);
        }

        System.out.println("Ghi chú: ");
        String note = sc.nextLine();
        Contract c;
        if (choice == 1) {
            c = new Contract(stats, account.getId(), account.getName(), name, money, phone, vay, due, interest, Contract.typeInterest.SIMPLE, note);
        } else {
            c = new Contract(stats, account.getId(), account.getName(), name, money, phone, vay, due, interest, Contract.typeInterest.COMPOUND, note);
        }
        ValidInfor.validate(c);

        return c;
    }
    /** Lưu hợp đồng vào ContractStorage và DataStore. */
    public static void save(Contract c) {
        try {
            if (c == null) return;
            ContractStorage.saveBorrow(
                    c.getStats().name(),
                    c.getAccountId(),
                    c.getAccountName(),
                    c.getName(),
                    c.getMoney(),
                    c.getPhoneNumber(),
                    c.getVayDate(),
                    c.getTraDate(),
                    c.getInterest(),
                    c.getType().name(),
                    c.getNote()
            );
            System.out.println("Đã lưu hợp đồng.");
        } catch (IOException e) {
            System.out.println("Lỗi khi lưu hợp đồng: " + e.getMessage());
        }
    }

    /** Hiển thị danh sách tài khoản và cho phép người dùng chọn hoặc huỷ. */
    static Account selectAccount(FinanceManager financeManager, Scanner sc) {
        List<Account> accounts = new ArrayList<>(financeManager.listAccounts());
        if (accounts.isEmpty()) {
            System.out.println("Chưa có tài khoản tài chính nào. Hãy tạo tài khoản trước khi lập hợp đồng.");
            return null;
        }
        System.out.println("Chọn tài khoản nguồn cho hợp đồng:");
        System.out.println("0) Huỷ");
        for (int i = 0; i < accounts.size(); i++) {
            Account acc = accounts.get(i);
            BigDecimal balance = acc.getBalance();
            System.out.printf("%d) %s - %s (Số dư: %s)%n",
                    i + 1, acc.getId(), acc.getName(), balance.toPlainString());
        }
        int choice = CheckInfor.checkOp(sc, 0, accounts.size());
        if (choice == 0) return null;
        return accounts.get(choice - 1);
    }
}
