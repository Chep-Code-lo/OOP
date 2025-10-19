package app.account;

import app.ui.*;
import java.util.Scanner;

/** Menu 2: Chuyển khoản giữa các tài khoản - format showMenu(Scanner, FinanceManager). */
public class TransferMenu {

    public void showMenu(Scanner sc, FinanceManager fm) {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("CHUYỂN KHOẢN");

            // cần tối thiểu 2 tài khoản ====
            int count = fm.listAccounts().size();
            if (count < 2) {
                if (count == 0) {
                    System.out.println("(Chưa có tài khoản nào. Vào menu 1 để tạo tài khoản trước.)");
                } else {
                    System.out.println("(Chỉ có 1 tài khoản. Cần ít nhất 2 tài khoản để chuyển khoản.)");
                }
                System.out.print("\nNhấn Enter để quay lại...");
                sc.nextLine();
                return;
            }

            System.out.println("1) Thực hiện chuyển khoản");
            System.out.println("2) Quay lại menu chính");
            System.out.print("Bạn muốn: ");
            String choice = sc.nextLine().trim();

            try{
                switch (choice) {
                    case "1"->{
                        Actions.doTransfer(sc, fm);
                        break;
                    }
                    case "2"->{
                        return;
                    }
                    default->{
                        System.out.println("Lựa chọn không hợp lệ. Nhấn Enter để thử lại...");
                        sc.nextLine();
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
