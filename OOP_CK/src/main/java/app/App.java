package app;

import app.account.FinanceManager;
import app.ui.*;
import java.util.Scanner;

/** Điểm vào chính: điều phối các menu con trong ứng dụng. */
public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FinanceManager financeManager = new FinanceManager();
        MenuAccount accountMenu = new MenuAccount(financeManager);
        Menutransaction mangeMenu = new Menutransaction(financeManager);
        MenuLoan loanMenu = new MenuLoan();
        ExportMenu expMenu = new ExportMenu();

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

            switch (sc.nextLine().trim()) {
                case "1" -> accountMenu.showMenu();
                case "2" -> mangeMenu.showMenu();
                case "3" -> loanMenu.showMenu(sc);
                case "4" -> {
                    ConsoleUtils.clear();
                    ConsoleUtils.pause(sc);
                }
                case "5" -> expMenu.show(sc);
                case "0" -> {
                    System.out.println("Pái pai nha");
                    return;
                }
                default -> {
                    System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 1 đến 6!");
                    ConsoleUtils.pause(sc);
                }
            }
        }
    }
}

