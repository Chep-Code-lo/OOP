package app.loan;

import app.model.Account;
import app.repository.ContractStorage;
import app.service.FinanceManager;
import app.util.ConsoleUtils;
import java.util.List;
import java.util.Scanner;

/** Cập nhật thông tin hợp đồng vay/cho vay. */
public class UpdateContact {
    /** Quy trình chọn hợp đồng, nhập giá trị mới và lưu lại. */
    public static void update(FinanceManager financeManager, Scanner sc) {
        try {
            List<ContractStorage.Contract> list = ContractStorage.loadAll();
            if (list.isEmpty()) {
                System.out.println("Hiện chưa có hợp đồng nào để cập nhật.");
                return;
            }

            // In danh sách: số thứ tự. id + tên người liên hệ
            ConsoleUtils.printHeader("DANH SÁCH HỢP ĐỒNG");
            System.out.println("0) Quay lại menu");
            for (int i = 0; i < list.size(); i++) {
                ContractStorage.Contract c = list.get(i);
                System.out.printf("%d) %s - %s (TK: %s)%n", i + 1, c.id, c.name, c.accountName == null || c.accountName.isBlank() ? c.accountId : c.accountName);
            }

            int choice = CheckInfor.checkOp(sc, 0, list.size());
            if (choice == 0) { return; }
            ContractStorage.Contract old = list.get(choice - 1);
            System.out.println("\n=== Cập nhật hợp đồng ===");
            System.out.println("(Enter để giữ nguyên giá trị cũ)");

            String status   = old.status;
            String accountId = old.accountId;
            String accountName = old.accountName;
            if ((accountId == null || accountId.isBlank()) && financeManager.listAccounts().isEmpty()) {
                System.out.println("Chú ý: Hợp đồng chưa gắn tài khoản. Hãy tạo tài khoản trước khi cập nhật.");
            }
            System.out.printf("Tài khoản hiện tại: %s (%s)%n",
                    accountName == null || accountName.isBlank() ? "(chưa có)" : accountName,
                    accountId == null || accountId.isBlank() ? "-" : accountId);
            System.out.print("Đổi tài khoản? (y/N): ");
            String changeAccount = sc.nextLine().trim();
            if (changeAccount.equalsIgnoreCase("y") || changeAccount.equalsIgnoreCase("yes")) {
                Account account = MakeContact.selectAccount(financeManager, sc);
                if (account != null) {
                    accountId = account.getId();
                    accountName = account.getName();
                } else {
                    System.out.println("Giữ nguyên tài khoản cũ.");
                }
            }

            String name     = prompt(sc, "Tên [" + old.name + "]: ");
            String phone    = prompt(sc, "SĐT [" + old.phone + "]: ");
            String traDate = prompt(sc, "Ngày vay (DD/MM/YYYY) [" + old.traDate + "]: ");
            String vayDate  = prompt(sc, "Hạn trả (DD/MM/YYYY) [" + old.traDate + "]: ");
            String moneyStr = prompt(sc, "Số tiền [" + old.money + "]: ");
            String interestStr = prompt(sc, "Lãi [" + old.interest + "]: ");
            String type = prompt(sc,"Loại Lãi ["+ old.type +"]: ");
            String note     = prompt(sc, "Ghi chú [" + old.note + "]: ");

            // Nếu để trống -> giữ nguyên
            if (name.isBlank())    name = old.name;
            if (phone.isBlank())   phone = old.phone;
            if (vayDate.isBlank()) vayDate = old.vayDate;
            if (traDate.isBlank()) traDate = old.traDate;
            if (moneyStr.isBlank()) moneyStr = old.money;
            if (interestStr.isBlank()) interestStr = old.interest;
            if (type.isBlank()) type = old.type;
            if (note.isBlank())    note = old.note;

            // Chuẩn hoá tiền: nếu người dùng gõ số trần, thêm " VND" để đồng bộ
            if (!moneyStr.contains("VND")) {
                moneyStr = moneyStr.trim() + " VND";
            }

            ContractStorage.Contract updated = new ContractStorage.Contract(
                    old.id, status, accountId, accountName, name, moneyStr, phone, vayDate, traDate, interestStr, type, note, old.createdAt
            );

            boolean ok = ContractStorage.updateByIndex(choice, updated);
            if (ok) System.out.println("Đã cập nhật và lưu lại hợp đồng.");
            else    System.out.println("Không cập nhật được (chỉ số không hợp lệ).");

        } catch (Exception e) {
            System.out.println("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    /** Đọc một dòng input, cho phép để trống để giữ nguyên. */
    private static String prompt(Scanner sc, String label) {
        System.out.print(label);
        return sc.nextLine();
    }

}
