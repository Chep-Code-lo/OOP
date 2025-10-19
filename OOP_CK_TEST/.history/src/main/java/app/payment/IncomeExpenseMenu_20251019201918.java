package app.payment;
import java.util.*;
public class IncomeExpenseMenu {
    public static IncomeExpenseReport.TxClass inputTxClass() {
        while (true) {
            System.out.println("Chọn phân loại:");
            System.out.println("1) Tất cả");
            System.out.println("2) Chỉ THU");
            System.out.println("3) Chỉ CHI");
            System.out.print("Chọn: ");
            String s = sc.nextLine().trim();
            switch (s) {
                case "1", "" -> { return IncomeExpenseReport.TxClass.ALL; }
                case "2" -> { return IncomeExpenseReport.TxClass.INCOME; }
                case "3" -> { return IncomeExpenseReport.TxClass.EXPENSE; }
                default -> System.out.println("Không hợp lệ, nhập lại.");
            }
        }
    }
}
