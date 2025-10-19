package app.loan;
import java.util.List;
import java.util.Scanner;

import app.util.ConsoleUtils;

public class UpdateContact{
    public static void update(Scanner sc) {
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
                System.out.printf("%d) %s - %s%n", i + 1, c.id, c.name);
            }

            int choice = CheckInfor.checkOp(sc, 0, list.size());
            if(choice == 0){return;}
            ContractStorage.Contract old = list.get(choice - 1);
            System.out.println("\n=== Cập nhật hợp đồng ===");
            System.out.println("(Enter để giữ nguyên giá trị cũ)");

            String status   = old.status;
            String name     = prompt(sc, "Tên [" + old.name + "]: ");
            String phone    = prompt(sc, "SĐT [" + old.phone + "]: ");
            String traDate = prompt(sc, "Ngày vay (DD/MM/YYYY) [" + old.traDate + "]: ");
            String vayDate  = prompt(sc, "Hạn trả (DD/MM/YYYY) [" + old.traDate + "]: ");
            String moneyStr = prompt(sc, "Số tiền [" + old.money + "]: ");
            String Interes = prompt(sc, "Lãi [" + old.interest + "]: ");
            String type =prompt(sc,"Loại Lãi ["+ old.type +"]: ");
            String note     = prompt(sc, "Ghi chú [" + old.note + "]: ");

            // Nếu để trống -> giữ nguyên
            if (name.isBlank())    name = old.name;
            if (phone.isBlank())   phone = old.phone;
            if (vayDate.isBlank()) vayDate = old.vayDate;
            if (traDate.isBlank()) traDate = old.traDate;
            if (moneyStr.isBlank()) moneyStr = old.money;
            if (Interes.isBlank()) Interes = old.interest;
            if (type.isBlank()) type = old.type;
            if (note.isBlank())    note = old.note;

            // Chuẩn hoá tiền: nếu người dùng gõ số trần, thêm " VND" để đồng bộ
            if (!moneyStr.contains("VND")) {
                moneyStr = moneyStr.trim() + " VND";
            }

            ContractStorage.Contract updated = new ContractStorage.Contract(
                    old.id, status, name, moneyStr, phone, vayDate, traDate, Interes, type, note, old.createdAt
            );

            boolean ok = ContractStorage.updateByIndex(choice, updated);
            if (ok) System.out.println("✔ Đã cập nhật và lưu lại hợp đồng.");
            else    System.out.println("✖ Không cập nhật được (chỉ số không hợp lệ).");

        } catch (Exception e) {
            System.out.println("✖ Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    private static String prompt(Scanner sc, String label) {
        System.out.print(label);
        return sc.nextLine();
    }

}
