package app.loan;

import java.util.Scanner;
import app.ui.*;

/** Đọc và kiểm tra thông tin liên hệ/khoản vay trước khi đẩy vào kho lưu trữ. */
public class ContactService{

    /** Thu thập dữ liệu từ console, áp dụng các kiểm tra định dạng và trả về Contact hợp lệ. */
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
            due = ConsoleIO.readNonBlank(sc, "Hạn trả nợ (DD/MM/YYYY): ");
            if (DateUtils.isValidDDMMYY(due)) break;
            System.out.println("Ngày không hợp lệ (ví dụ 31/04/2024 sẽ bị từ chối). Nhập lại!");
        }

        double interest = ConsoleIO.readPositiveDouble(sc, "Lãi suất (%): ");

        System.out.println("Ghi chú: ");
        String note = sc.nextLine();
        Contact c = new Contact(stats, name, money, phone, due, interest, note);
        ContactValidator.validate(c);
        return c;
    }

    /** Đưa Contact đã hợp lệ vào hàng đợi để ContractStorage xử lý. */
    public void queueForSave(Contact c){
        ContractStorage.queue(c);
    }
}
