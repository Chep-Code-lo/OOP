package app.loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class DateService {
    public static void Datecheck() {
        Scanner sc = new Scanner(System.in);
        try {
            List<ContractStorage.Contract> list = ContractStorage.loadAll();
            if (list.isEmpty()) {
                System.out.println("Hiện chưa có hợp đồng nào để tính lãi.");
                return;
            }

            // In danh sách: số thứ tự. id + tên người liên hệ
            System.out.println("=== Danh sách hợp đồng ===");
            System.out.println("0) Quay lại menu");
            for (int i = 0; i < list.size(); i++) {
                ContractStorage.Contract c = list.get(i);
                System.out.printf("%d) %s - %s%n", i + 1, c.id, c.name);
            }

            int choice = CheckInfor.checkOp(sc, 0, list.size());
            if (choice == 0) {
                return;
            }
            ContractStorage.Contract interestMoney = list.get(choice - 1);
            LocalDate repay = ConvertDetail.parseDate(interestMoney.traDate);
            LocalDate now = LocalDate.now();
            int Deadline = InterestService.daysBetween(now, repay);
            System.out.println("Bạn còn "+Deadline+" ngày để đến hạn của hợp đồng thôi đó!!!!" );
        } catch (Exception e) {
            System.out.println("✖ Lỗi khi xác định ngày: " + e.getMessage());
        }
    }
}
