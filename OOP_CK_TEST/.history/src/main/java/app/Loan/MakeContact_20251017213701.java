package app.loan;
import java.io.IOException;
import java.util.Scanner;
class MakeContact{
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
    private static Contract Create(Scanner sc, Contract.Stats stats) {
        String name = readInfor.readNonBlank(sc, "Tên người liên hệ: ");
        String money = readInfor.readNonBlank(sc, "Số tiền nợ: ");

        String phone;
        while (true) {
            phone = readInfor.readNonBlank(sc, "Số điện thoại (10 số): ");
            if (ValidInfor.isValidPhone10(phone)) break;
            System.out.println("Số điện thoại không hợp lệ. Ví dụ: 0912345678");
        }
        String vay;
        while (true) {
            vay=readInfor.readNonBlank(sc,"Ngày tạo hợp đồng (DD/MM/YYYY): ");
            if(CheckInfor.DateUtils.isValidDDMMYY(vay)) break;
            System.out.println("Ngày không hợp lệ (ví dụ 34/4/24 sẽ bị từ chối). Nhập lại!");
        }
        String due;
        while (true) {
            due = readInfor.readNonBlank(sc, "Hạn trả nợ (DD/MM/YYYY): ");
            if (CheckInfor.DateUtils.isValidDDMMYY(due)) break;
            System.out.println("Ngày không hợp lệ (ví dụ 31/04/24 sẽ bị từ chối). Nhập lại!");
        }

        double interest = readInfor.readPositiveDouble(sc, "Lãi suất (%): ");
        int choice = 0;
        if(interest!=0){
            System.out.println("Hãy chọn loại lãi suất  ");
            System.out.println("1. Lãi đơn");
            System.out.println("2. Lãi kép");
            choice = CheckInfor.checkOp(sc, 1, 2);
        }

        System.out.println("Ghi chú: ");
        String note = sc.nextLine();
        Contract c;
        if(choice == 1 ) c = new Contract(stats, name, money, phone,vay , due, interest, Contract.typeInterest.SIMPLE, note);
        else c = new Contract(stats, name, money, phone,vay , due, interest, Contract.typeInterest.COMPOUND, note);
        ValidInfor.validate(c);

        return c;
    }
    private static void save(Contract c) {
        try {
            ContractStorage.saveBorrow(
                    c.getStats().name(),
                    c.getName(),
                    c.getMoney(),
                    c.getPhoneNumber(),
                    c.getVayDate(),
                    c.getTraDate(),
                    c.getInterest(),
                    c.getType().name(),
                    c.getNote()
            );
            System.out.println("✔ Đã lưu hợp đồng.");
        } catch (IOException e) {
            System.out.println("✖ Lỗi khi lưu hợp đồng: " + e.getMessage());
        }
    }
}