package app.ui;

import java.util.Scanner;

import app.account.*;

public class MenuAccount {
    private final FinanceManager fm;

    public MenuAccount(FinanceManager fm) {
        this.fm = fm;
    }

    public void showMenu() {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("QUẢN LÝ TÀI CHÍNH CÁ NHÂN");
            System.out.println("1) Thêm/xóa/chỉnh sửa tài khoản");
            System.out.println("2) Chuyển khoản giữa các tài khoản");
            System.out.println("3) Theo dõi số dư cho từng tài khoản");
            System.out.println("4) Báo cáo tài chính theo tài khoản");
            System.out.println("0) Thoát");
            System.out.print("Bạn muốn : ");

            Scanner sc = new Scanner(System.in);

            switch(sc.nextLine().trim()) {
                case "1"->{
                    AccountMenu m = new AccountMenu();
                    m.showMenu(sc, fm);
                    System.out.println("Nhấn Enter để quay lại menu...");
                    sc.nextLine();
                    break;
                }
                case "2"->{
                    TransferMenu m = new TransferMenu();
                    m.showMenu(sc, fm);
                    System.out.println("Nhấn Enter để quay lại menu...");
                    sc.nextLine();
                    break;
                }
                case "3"->{
                    ReportMenu m = new ReportMenu();
                    m.showBalances(sc, fm);
                    System.out.println("Nhấn Enter để quay lại menu...");
                    sc.nextLine();
                    break;
                }
                case "4" ->{
                    ReportMenu m = new ReportMenu();
                    m.showReport(sc, fm);
                    System.out.println("Nhấn Enter để quay lại menu...");
                    sc.nextLine();
                    break;
                }
                case "0" ->{
                    System.out.println("Tạm biệt!");
                    return;
                }
                default ->{
                    System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 5!");
                }
            }
        }
    }
}
