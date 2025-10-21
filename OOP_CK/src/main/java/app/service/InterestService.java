package app.service;

import app.loan.CheckInfor;
import app.loan.ConvertDetail;
import app.repository.ContractStorage;
import app.util.ConsoleUtils;
import app.util.DateUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;


public class InterestService {

    private InterestService() {}

    /** Hiển thị danh sách hợp đồng và tính toán số tiền phải trả theo từng mô hình lãi. */
    public static void showMenu(Scanner sc) {
        if (sc == null) throw new IllegalArgumentException("scanner");
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

            double interestRate = ConvertDetail.parseInterest(interestMoney.interest);
            String realType = ConvertDetail.parseType(interestMoney.type, interestRate);

            System.out.println("Đây là mô hình lãi "+realType);

            LocalDate borrow = DateUtils.parseDate(interestMoney.vayDate);
            LocalDate repay = DateUtils.parseDate(interestMoney.traDate);
            LocalDate now = LocalDate.now();

            BigDecimal principal = ConvertDetail.parseAmount(interestMoney.money);
            BigDecimal moneyDeadline;
            BigDecimal moneyNow;

            if (realType.equalsIgnoreCase("không tính lãi") || interestRate <= 0) {
                System.out.println("Số tiền phải trả khi đến hạn ("+ interestMoney.traDate +") là " + principal + " VNĐ");
                System.out.println("Khoản vay này không tính lãi.");
                return;
            }

            if (realType.equalsIgnoreCase("lãi đơn")) {
                moneyDeadline = InterestService.calcuMoneySimple(principal, interestRate, borrow, repay);
                moneyNow = InterestService.calcuMoneySimple(principal, interestRate, borrow, now);
                System.out.println("Số tiền phải trả khi đến hạn ("+ interestMoney.traDate +") là " + moneyDeadline.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + " VNĐ");
                System.out.println("Số tiền phải trả cho đến hiện tại ("+ now +") là " + moneyNow.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + " VNĐ");
                return;
            }

            if (realType.equalsIgnoreCase("lãi kép")) {
                moneyDeadline = InterestService.calcuMoneyCompound(principal, interestRate, borrow, repay);
                moneyNow = InterestService.calcuMoneyCompound(principal, interestRate, borrow, now);
                System.out.println("Số tiền phải trả khi đến hạn ("+ interestMoney.traDate +") là " + moneyDeadline.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + " VNĐ");
                System.out.println("Số tiền phải trả cho đến hiện tại ("+ now +") là " + moneyNow.setScale(0, java.math.RoundingMode.DOWN).toPlainString() + " VNĐ");
                return;
            }

            System.out.println("Không xác định được mô hình lãi suất, vui lòng kiểm tra lại dữ liệu.");

        } catch (Exception e) {
            System.out.println("✖ Lỗi khi tính lãi: " + e.getMessage());
        }
    }


    /** Tính tiền phải trả với lãi đơn theo số ngày giữa hai mốc. */
    private static BigDecimal calcuMoneySimple(BigDecimal money, double interest, LocalDate borrow, LocalDate repay) {
        double r = interest / 100.0;
        int days = daysBetween(borrow, repay);
        double factor = r * days;
        BigDecimal in =money.multiply(BigDecimal.valueOf(factor));
        return money.add(in);
    }

    /** Tính tiền phải trả với lãi kép theo số ngày giữa hai mốc. */
    private static BigDecimal calcuMoneyCompound (BigDecimal money, double interest, LocalDate borrow, LocalDate repay){
        double r = interest / 100.0;
        int days = daysBetween(borrow, repay);
        double factor = Math.pow(1 + r, days) - 1; // chỉ LÃI
        BigDecimal in = money.multiply(BigDecimal.valueOf(factor));
        return money.add(in);
    }

    /** Đếm số ngày giữa hai mốc (âm -> 0). */
    public static int daysBetween(LocalDate a, LocalDate b) {
        return (int)Math.max(0, ChronoUnit.DAYS.between(a, b));
    }
}
