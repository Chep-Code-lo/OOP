package app.main;
import app.service.*;
import app.util.*;
import app.ui.menu.*;
import app.ui.menu.AccountMenu;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FinanceManager financeManager = new FinanceManager();

        AccountMenu accountMenu = new AccountMenu(financeManager, sc);
        TransactionMenu mangeMenu = new TransactionMenu(financeManager, sc);
        MenuLoan loanMenu = new MenuLoan();
        MenuPayment paymentMenu = new MenuPayment();
        ExportMenu expMenu = new ExportMenu(sc);

        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("MENU CHÍNH");
            System.out.println("1. Quản lý tài khoản tài chính");
            System.out.println("2. Quản lý giao dịch thu chi");
            System.out.println("3. Quản lý khoản vay và cho vay");
            System.out.println("4. Báo cáo và thống kê tài chính");
            System.out.println("5. Xuất file");
            System.out.println("0. Thoát");
            System.out.print("Bạn muốn : ");

            try{
                switch (sc.nextLine().trim()) {
                    case "1" -> accountMenu.showMenu();
                    case "2" -> mangeMenu.showMenu();
                    case "3" -> loanMenu.showMenu();
                    case "4" -> paymentMenu.showMenu();
                    case "5" -> expMenu.show();
                    case "0" -> {
                        System.out.println("Pái pai nha");
                        return;
                    }
                    default -> {
                        System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 5!");
                        ConsoleUtils.pause(sc);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(sc);
            }
        }
    }
}
