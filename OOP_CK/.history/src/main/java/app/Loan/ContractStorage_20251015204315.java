package app.loan;

import app.store.DataStore;
import java.io.IOException;
import java.nio.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Lưu trữ tạm thời thông tin hợp đồng vay và đồng bộ sang DataStore dưới dạng map.
 * Sử dụng hàng đợi PENDING cho các contact nhập từ UI trước khi ghi ra CSV.
 */
public class ContractStorage {
    private static final Path COUNTER_PATH = Paths.get("data", "id_counters.properties");
    private static final int  ID_WIDTH     = 4; // -> Co0001, Ch0001 (đổi 5 nếu muốn Co00001)

    private static final List<Contact> PENDING = new ArrayList<>();

    /** Nhận contact nhập từ UI và đưa vào hàng đợi chờ ghi. */
    public static synchronized void queue(Contact contact) {
        PENDING.add(contact);
    }

    /** Đẩy toàn bộ contact đang chờ vào DataStore, trả về số bản ghi đã xử lý. */
    public static synchronized int flushPending() throws IOException {
        int flushed = 0;
        while (!PENDING.isEmpty()) {
            Contact contact = PENDING.remove(0);
            saveBorrow(
                    contact.getStats().name(),
                    contact.getName(),
                    contact.getMoney(),
                    contact.getPhoneNumber(),
                    contact.getDueDate(),
                    contact.getInterest(),
                    contact.getNote()
            );
            flushed++;
        }
        return flushed;
    }

    /** Ghi một khoản vay (kể cả từ flush) vào DataStore với map các trường chuẩn hóa. */
    public static void saveBorrow(String status,
                                  String name,
                                  double money,
                                  String phone,
                                  String dueDate,
                                  double interest,
                                  String note) throws IOException {
        String id = nextIdForStatus(status);
        String createdAt = LocalDateTime.now().toString();

        var row = new LinkedHashMap<String, Object>();
        row.put(DataStore.LoanFields.ID, id);
        row.put(DataStore.LoanFields.STATUS, status);
        row.put(DataStore.LoanFields.NAME, name);
        row.put(DataStore.LoanFields.AMOUNT, money);
        row.put(DataStore.LoanFields.PHONE, phone);
        row.put(DataStore.LoanFields.DUE_DATE, dueDate);
        row.put(DataStore.LoanFields.INTEREST, interest);
        row.put(DataStore.LoanFields.NOTE, note);
        row.put(DataStore.LoanFields.CREATED_AT, createdAt);
        DataStore.addLoan(row);
    }

    // ====== Sinh ID tuần tự theo stats ======
    private static synchronized String nextIdForStatus(String status) throws IOException {
        String prefix = statusPrefix(status); // CoNo -> "Co", ChNo -> "Ch"

        Properties p = new Properties();
        if (Files.exists(COUNTER_PATH)) {
            try (var in = Files.newInputStream(COUNTER_PATH)) { p.load(in); }
        }
        int last = Integer.parseInt(p.getProperty(prefix, "0"));
        int next = last + 1;

        p.setProperty(prefix, Integer.toString(next));
        Files.createDirectories(COUNTER_PATH.getParent());
        try (var out = Files.newOutputStream(COUNTER_PATH)) {
            p.store(out, "ID counters per prefix (Co/Ch)");
        }

        return prefix + String.format("%0" + ID_WIDTH + "d", next);
    }

    private static String statusPrefix(String status) {
        if (status == null) return "Co";
        switch (status) {
            case "CoNo": return "Co";
            case "ChNo": return "Ch";
            default:     return status.substring(0, Math.min(2, status.length()));
        }
    }
}
