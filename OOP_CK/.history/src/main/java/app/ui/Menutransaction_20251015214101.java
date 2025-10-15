package app.ui;

import app.account.FinanceManager;
import app.transaction.*;
import java.util.Scanner;

public class Menutransaction {
    private final App app;

    public Menutransaction(FinanceManager fm) {
        TransactionService service = new TransactionService(fm);
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
            System.out.println("0) Thoát");
            System.out.print("Bạn muốn :  ");

            try {
                switch (sc.nextLine().trim()) {
                    case "1"->{
                        app.showBalances();
                        ConsoleUtils.pause(sc);
                        break;
                    }
                    case "2"->{
                        app.addTxCLI(sc);
                        ConsoleUtils.pause(sc);
                        break;
                    }
                    case "3"->{
                        app.listTxCLI(sc);
                        ConsoleUtils.pause(sc);
                        break;
                    }
                    case "4"->{
                        app.editTxCLI(sc);
                        ConsoleUtils.pause(sc);
                        break;
                    }
                    case "5"->{
                        app.deleteTxCLI(sc);
                        ConsoleUtils.pause(sc);
                        break;
                    }
                    case "0"->{
                        System.out.println("Bye!");
                        return;
                    }
                    default->{
                        System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 1 đến 6!");
                        ConsoleUtils.pause(sc);
                    }
                }
            } catch (Exception e) {
                System.out.println("!! Lỗi: " + e.getMessage());
                ConsoleUtils.pause(sc);
            }
        }
    }
}
