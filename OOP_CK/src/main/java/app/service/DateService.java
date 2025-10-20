package app.service;

import app.loan.CheckInfor;
import app.repository.ContractStorage;
import app.util.ConsoleUtils;
import app.util.DateUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/** Dịch vụ nhắc nhở hạn trả hợp đồng vay. */
public class DateService {
    /** Hiển thị danh sách hợp đồng và tính số ngày còn lại đến hạn trả. */
    public static void Datecheck(Scanner sc) {
        if (sc == null) throw new IllegalArgumentException("scanner");
        try {
            List<ContractStorage.Contract> list = ContractStorage.loadAll();
            if (list.isEmpty()) {
                System.out.println("Hiện chưa có hợp đồng nào để tính lãi.");
                return;
            }

            ConsoleUtils.printHeader("DANH SÁCH HỢP ĐỒNG");
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

            LocalDate repay = DateUtils.parseDate(interestMoney.traDate);
            LocalDate now = LocalDate.now();

            int deadline = InterestService.daysBetween(now, repay);
            System.out.println("Bạn còn " + deadline + " ngày để đến hạn của hợp đồng thôi đó!!!!");
        } catch (Exception e) {
            System.out.println("✖ Lỗi khi xác định ngày: " + e.getMessage());
        }
    }
}
