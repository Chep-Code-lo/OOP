package app.account;

import app.ui.*;
import java.util.Scanner;


public class AccountMenu {

    public void showMenu(Scanner sc, FinanceManager fm) {
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("TÀI KHOẢN");
            System.out.println("1) Thêm tài khoản");
            System.out.println("2) Đổi tên tài khoản");
            System.out.println("3) Xóa tài khoản (chỉ khi số dư = 0)");
            System.out.println("4) Hiển thị danh sách tài khoản");
            System.out.println("0) Quay lại");
            System.out.print("Chọn (0-4): ");
            try {
                switch(sc.nextLine().trim()){
                    case "1"->{
                        Actions.add(sc, fm);
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case "2"->{
                        Actions.rename(sc, fm);
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case "3"->{
                        Actions.delete(sc, fm);
                        System.out.println("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case "4"->{
                        ConsoleUtils.clear();
                        ConsoleUtils.printHeader("DANH SÁCH TÀI KHOẢN");
                        Actions.listAccounts(fm);
                        System.out.print("Nhấn Enter để quay lại menu...");
                        sc.nextLine();
                        break;
                    }
                    case" 0"-> {
                        return;
                    } 
                    default->{
                        System.out.println("Giá trị không hợp lệ. Vui lòng nhập số từ 0 đến 4.");
                        ConsoleUtils.pause(sc);
                    }
                }
            } catch (Exception e) {
                System.out.println("Lỗi: " + e.getMessage());
                ConsoleUtils.pause(sc);
            }
        }
    }
}
