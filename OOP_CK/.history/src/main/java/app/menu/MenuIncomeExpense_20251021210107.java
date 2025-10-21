package app.menu;

import app.report.IncomeExpenseReport;
import app.util.ConsoleUtils;
import java.util.Scanner;
/** Menu phụ hỗ trợ chọn phân loại giao dịch cho báo cáo Thu - Chi. */
public class MenuIncomeExpense {

    public static IncomeExpenseReport.TxClass inputTxClass() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Chọn phân loại:");
            System.out.println("1) Tất cả");
            System.out.println("2) Chỉ THU");
            System.out.println("3) Chỉ CHI");
            System.out.print("Chọn: ");
            
            try{
                switch (sc.nextLine().trim()){
                    case "1", "" -> { return IncomeExpenseReport.TxClass.ALL; }
                    case "2" -> { return IncomeExpenseReport.TxClass.INCOME; }
                    case "3" -> { return IncomeExpenseReport.TxClass.EXPENSE; }
                    default -> System.out.println("Không hợp lệ, nhập lại.");
                }
            }
            catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(sc);
            }
        }
    }
}
