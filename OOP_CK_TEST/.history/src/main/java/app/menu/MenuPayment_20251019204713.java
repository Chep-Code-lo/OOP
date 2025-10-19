package app.menu;

import java.util.Scanner;
import app.util.*;
import app.payment.*;

public class MenuPayment {
    public static void start() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            ConsoleUtils.clear();
            ConsoleUtils.printHeader("MENU BÁO CÁO");
            System.out.println("1) Báo cáo Thu - Chi (lọc theo ngày, danh mục, phân loại)");
            System.out.println("2) Báo cáo Khoản Vay (lọc theo hạn trả, trạng thái)");
            System.out.println("3) Báo cáo Tài Khoản");
            System.out.println("0) Thoát");
            System.out.print("Chọn: ");

            try{
                switch (sc.nextLine().trim()){
                    case "1" -> {
                        Action.runIncomeExpenseWithFilters(); // Thu - Chi: lọc Ngày + Danh mục + Phân loại
                        ConsoleUtils.pause(sc);
                    }
                    case "2" -> {
                        Action.runLoanWithFilters();          // Khoản vay: lọc Ngày (hạn trả) + Trạng thái
                        ConsoleUtils.pause(sc);
                    }
                    case "3" -> {
                        AccountReport.run();           // Tài khoản
                        ConsoleUtils.pause(sc);
                    }
                    case "0" -> {
                        System.out.println("Tạm biệt!");
                        return; // thoát menu báo cáo
                    }
                    default -> {
                        System.out.println("Lựa chọn không hợp lệ. Hãy nhập từ 0 đến 5!");
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
