package app.ui;

import app.manage.*;
import java.util.Scanner;

public class Menumanage {
    private final App app;

    public Menumanage() {
        TransactionService service = new TransactionService();
        this.app = new App(service);
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Quản Lý Giao Dịch");
        while (true) {
            System.out.println("1) Xem số dư");
            System.out.println("2) Thêm giao dịch");
            System.out.println("3) Xem lịch sử giao dịch");
            System.out.println("4) Sửa giao dịch");
            System.out.println("5) Xóa giao dịch");
            System.out.println("q) Thoát");
            System.out.print("> ");

            try {
                switch (sc.nextLine().trim()) {
                    case "1": app.showBalances(); ConsoleUtils.pause(sc);;
                    case "2": app.addTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "3": app.listTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "4": app.editTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "5": app.deleteTxCLI(sc); ConsoleUtils.pause(sc);;
                    case "q": case "Q": System.out.println("Bye!"); return;
                    default: System.out.println("Lệnh không hợp lệ.");
                }
            } catch (Exception e) { System.out.println("!! Lỗi: " + e.getMessage()); }
        }
    }

}
