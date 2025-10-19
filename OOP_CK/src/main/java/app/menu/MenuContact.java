package app.menu;

import app.loan.CheckInfor;
import app.loan.MakeContact;
import app.model.Contract;
import java.util.Scanner;

public final class MenuContact {
    private MenuContact() {}

    public static void showMenu(Scanner sc) {
        if (sc == null) throw new IllegalArgumentException("scanner");
        while (true) {
            System.out.println("==========Menu Contact==========");
            System.out.println("1. Cho chủ nợ");
            System.out.println("2. Cho con nợ");
            System.out.println("3. Quay lại menu");
            int option = CheckInfor.checkOp(sc, 1, 3);
            switch (option) {
                case 1 -> {
                    Contract c = MakeContact.Create(sc, Contract.Stats.ChNo);
                    MakeContact.save(c);
                }
                case 2 -> {
                    Contract c = MakeContact.Create(sc, Contract.Stats.CoNo);
                    MakeContact.save(c);
                }
                case 3 -> {
                    return;
                }
                default -> {}
            }
        }
    }
}
