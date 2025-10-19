package app.ui.menu;
import java.util.Scanner;

import app.loan.*;

public class MenuContact {
    private static boolean use = true;
    private static int option;
    public static void showMenu() {
        Scanner sc = new Scanner(System.in);
        while (use) {
            System.out.println("==========Menu Contact==========");
            System.out.println("1. Cho chủ nợ");
            System.out.println("2. Cho con nợ");
            System.out.println("3. Quay lại menu");
            option = CheckInfor.checkOp(sc, 1, 3);
            switch (option) {
                case 1: {
                    Contract c = MakeContact.Create(sc, Contract.Stats.ChNo);
                    MakeContact.save(c);
                    break;
                }
                case 2: {
                    Contract c = MakeContact.Create(sc, Contract.Stats.CoNo);
                    MakeContact.save(c);
                    break;
                }
                case 3:{
                    return;
                }
            }
        }
    }
}
