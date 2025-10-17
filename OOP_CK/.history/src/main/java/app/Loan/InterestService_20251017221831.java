package app.loan;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.LocalDate;
import app.ui.*;

/**
 * Service tính lãi cho hợp đồng đọc từ ContractStorage.
 * - Hỗ trợ lãi ĐƠN (SIMPLE) và lãi KÉP (COMPOUND)
 * - Hỗ trợ cả khoản VAY (bạn phải trả) và CHO VAY (người khác trả cho bạn)
 * - Tính tại mốc bất kỳ (asOfDate) và tại ngày đến hạn (dueDate)
 */
public class InterestService {

    private InterestService() {}

    public static void Menu() {
        Scanner sc = new Scanner(System.in);
        try {
            List<ContractStorage.Contract> list = ContractStorage.loadAll();
            if (list.isEmpty()) {
                System.out.println("Hiện chưa có hợp đồng nào để tính lãi.");
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

            if(choice == 0){
                return;
            }
            ContractStorage.Contract interestMoney = list.get(choice - 1);

            ConsoleUtils.printHeader("LÃI HỢP ĐỒNG");

            String realType= ConvertDetail.parseType(interestMoney.type, Double.parseDouble(interestMoney.interest));

            System.out.println("Đây là mô hình lãi "+realType);

            LocalDate borrow = DateUtils.parseDate(interestMoney.vayDate);
            LocalDate repay = DateUtils.parseDate(interestMoney.traDate);
            LocalDate now = LocalDate.now();

            double inMoney =ConvertDetail.parseInterest(interestMoney.interest);
            BigDecimal realMoney = ConvertDetail.parseAmount(interestMoney.money);

            BigDecimal moneyDeadline; 
            BigDecimal moneyNow;

            if(realType.matches("không tính lãi")){}

            if(realType.matches("lãi đơn")){
                moneyDeadline = InterestService.calcuMoneySimple(realMoney, inMoney,borrow, repay);
                moneyNow = InterestService.calcuMoneySimple(realMoney, inMoney,borrow, now);
                System.out.println("Số tiền phải trả khi đến hạn ("+ interestMoney.traDate +") là " + moneyDeadline + "VNĐ");
                System.out.println("Số tiền phải trả cho đến hiện tại ("+ now +") là " + moneyNow+ "VNĐ");
            }

            if(realType.matches("lãi kép")){
                moneyDeadline = InterestService.calcuMoneyCompound(realMoney, inMoney, borrow, repay);
                moneyNow = InterestService.calcuMoneyCompound(realMoney, inMoney,borrow, now);
                System.out.println("Số lãi khi đến hạn ("+ interestMoney.traDate +") là " + moneyDeadline + "VNĐ");
                System.out.println("Số tiền phải trả cho đến hiện tại là " + moneyNow+ "VNĐ");
            }

        } catch (Exception e) {
            System.out.println("✖ Lỗi khi tính lãi: " + e.getMessage());
        }
    }


    private static BigDecimal calcuMoneySimple(BigDecimal money, double interest, LocalDate borrow, LocalDate repay) {
        double r = interest / 100.0;
        int days = daysBetween(borrow, repay);
        double factor = r * days;
        BigDecimal in =money.multiply(BigDecimal.valueOf(factor));
        return money.add(in);
    }

    private static BigDecimal calcuMoneyCompound (BigDecimal money, double interest, LocalDate borrow, LocalDate repay){
        double r = interest / 100.0;
        int days = daysBetween(borrow, repay);
        double factor = Math.pow(1 + r, days) - 1; // chỉ LÃI
        BigDecimal in = money.multiply(BigDecimal.valueOf(factor));
        return money.add(in);
    }

    public static int daysBetween(LocalDate a, LocalDate b) {
        return (int)Math.max(0, ChronoUnit.DAYS.between(a, b));
    }
}
