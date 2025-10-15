package app.ui;

import app.transaction.*;
import java.util.Scanner;

public class Menutransaction {
    private final App app;

    public Menutransaction() {
        TransactionService service = new TransactionService();
        this.app = new App(service);
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("QUẢN LÝ GIAO DỊCH");
            System.out.println("1) Xem số dư");
            System.out.println("2) Thêm giao dịch");
            System.out.println("3) Xem lịch sử giao dịch");
            System.out.println("4) Sửa giao dịch");
            System.out.println("5) Xóa giao dịch");
            System.out.println("6) Thoát");
            System.out.print("Bạn muốn :  ");

            try {
                switch (sc.nextLine().trim()) {
                    case "1": app.showBalances(); ConsoleUtils.pause(sc);;
                    case "2": app.addTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "3": app.listTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "4": app.editTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "5": app.deleteTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "6": System.out.println("Bye!"); return;
                    default: System.out.println("Vui lòng chọn số từ 1-6!");
                }
            } catch (Exception e) { System.out.println("!! Lỗi: " + e.getMessage()); }
        }
    }

}
