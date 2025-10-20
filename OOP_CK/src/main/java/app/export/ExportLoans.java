package app.export;
import app.repository.DataStore;
import java.nio.file.Path;
import java.util.*;

/** Xuất CSV: Khoản vay / cho vay (đọc trực tiếp từ DataStore) */
public final class ExportLoans {
    private ExportLoans() {}
    /** Thu thập dữ liệu khoản vay/cho vay và ghi file CSV tương ứng. */
    public static Path export() throws Exception {
        String[] headers = {
            "ID","Trạng thái","Tên liên hệ","Số tiền","Số điện thoại",
            "Ngày tạo hợp đồng","Hạn trả","Lãi suất","Loại lãi","Ghi chú","Thời điểm tạo"
        };
        List<String[]> rows = new ArrayList<>();
        for (var l : DataStore.loans()) {
            String amount = value(l, DataStore.LoanFields.AMOUNT);
            rows.add(new String[]{
                value(l, DataStore.LoanFields.ID),
                value(l, DataStore.LoanFields.STATUS),
                value(l, DataStore.LoanFields.NAME),
                amount.isBlank() ? "" : amount + " VND",
                value(l, DataStore.LoanFields.PHONE),
                value(l, DataStore.LoanFields.BORROW_DATE),
                value(l, DataStore.LoanFields.DUE_DATE),
                value(l, DataStore.LoanFields.INTEREST),
                value(l, DataStore.LoanFields.TYPE),
                value(l, DataStore.LoanFields.NOTE),
                value(l, DataStore.LoanFields.CREATED_AT)
            });
        }
        return CsvExporter.writeCsv("loans.csv", headers, rows);
    }

    /** Trích giá trị từ map và chuyển sang chuỗi an toàn cho CSV. */
    private static String value(java.util.Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? "" : v.toString();
    }
}
