package app.loan;
import java.util.Scanner;

public class ContactService{

    public Contact createFromConsole(Scanner sc, Contact.Stats stats){
        String name = ConsoleIO.readNonBlank(sc, "Tên người liên hệ: ");
        double money = ConsoleIO.readPositiveDouble(sc, "Số tiền (VND): ");

        String phone;
        while(true){
            phone = ConsoleIO.readNonBlank(sc, "Số điện thoại (10 số): ");
            if (ContactValidator.isValidPhone10(phone)) break;
            System.out.println("Số điện thoại không hợp lệ. Ví dụ: 0912345678");
        }

        String due;
        while(true){
            due = ConsoleIO.readNonBlank(sc, "Hạn trả nợ (DD/MM/YY): ");
            if (DateUtils.isValidDDMMYY(due)) break;
            System.out.println("Ngày không hợp lệ (ví dụ 31/04/24 sẽ bị từ chối). Nhập lại!");
        }

        double interest = ConsoleIO.readPositiveDouble(sc, "Lãi suất (%): ");

        System.out.println("Ghi chú: ");
        String note = sc.nextLine();
        return c;
    }
}
