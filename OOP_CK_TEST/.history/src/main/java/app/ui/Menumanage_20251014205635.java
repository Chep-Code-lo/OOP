package app.ui;
import app.manage.*;
import java.util.Scanner;

public class Menumanage {
    public void run() {
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
                    case "1": showBalances(); break;
                    case "2": addTxCLI(sc); break;
                    case "3": listTxCLI(sc); break;
                    case "4": editTxCLI(sc); break;
                    case "5": deleteTxCLI(sc); break;
                    case "q": case "Q": System.out.println("Bye!"); return;
                    default: System.out.println("Lệnh không hợp lệ.");
                }
            } catch (Exception e) { System.out.println("!! Lỗi: " + e.getMessage()); }
        }
    }

}
