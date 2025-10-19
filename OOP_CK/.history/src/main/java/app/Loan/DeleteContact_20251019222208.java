package app.loan;
import java.util.List;
import java.util.Scanner;
import app.repository.*;
import app.util.ConsoleUtils;

import java.io.IOException;

public class DeleteContact {
    public static void delete(Scanner sc) {
        try {
            List<ContractStorage.Contract> list = ContractStorage.loadAll();
            if (list.isEmpty()) {
                System.out.println("Hiện chưa có hợp đồng nào để xóa.");
                return;
            }

            ConsoleUtils.printHeader("DANH SÁCH HỢP ĐỒNG");
            for (int i = 0; i < list.size(); i++) {
                ContractStorage.Contract c = list.get(i);
                System.out.printf("%d) %s - %s%n", i + 1, c.id, c.name);
            }

            int choice = LoanInputCheck.checkOp(sc, 1, list.size());
            ContractStorage.Contract victim = list.get(choice - 1);

            // Xác nhận
            String dispId = displayId(victim.id, choice);
            if (!confirm(sc, "Bạn chắc muốn xóa [" + dispId + "] - " + victim.name + " ? (y/N): ")) {
                System.out.println("Đã hủy xóa.");
                return;
            }
            // Xóa và lưu lại CSV
            list.remove(choice - 1);
            ContractStorage.saveAll(list);
            boolean ok=true;
            if(ok) System.out.println("✔ Đã xóa và lưu lại danh sách hợp đồng.");
            else System.out.println("Không xóa được");

        } catch (IOException e) {
            System.out.println("✖ Lỗi khi xóa: " + e.getMessage());
        }
    }

    /** ID hiển thị*/
    private static String displayId(String rawId, int oneBasedIndex) {
        if (rawId == null || rawId.isBlank()) return String.valueOf(oneBasedIndex);
        String id = rawId.trim();

        return id;
    }

    private static boolean confirm(Scanner sc, String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim().toLowerCase();
        return s.equals("y") || s.equals("yes") || s.equals("1");
    }
}
