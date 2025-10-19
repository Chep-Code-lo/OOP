package app.export;
import app.store.DataStore;
import java.nio.file.Path;
import java.util.*;

/** Xuất CSV: Giao dịch (đọc trực tiếp từ DataStore) */
public final class ExportTransactions {
    private ExportTransactions() {}
    public static Path export() throws Exception {
        String[] headers = {"Ngày","Loại","Số tiền","Tài khoản","Danh mục","Ghi chú"};
        List<String[]> rows = new ArrayList<>();
        for (var t : DataStore.transactions()) {
            rows.add(new String[]{
                value(t, DataStore.TransactionFields.DATE),
                value(t, DataStore.TransactionFields.TYPE),
                value(t, DataStore.TransactionFields.AMOUNT),
                preferName(t),
                value(t, DataStore.TransactionFields.CATEGORY),
                value(t, DataStore.TransactionFields.NOTE)
            });
        }
        return CsvExporter.writeCsv("transactions.csv", headers, rows);
    }

    /** Ưu tiên tên hiển thị; fallback sang ID nếu chưa có. */
    private static String preferName(java.util.Map<String, Object> row) {
        String name = value(row, DataStore.TransactionFields.ACCOUNT_NAME);
        if (!name.isBlank()) return name;
        return value(row, DataStore.TransactionFields.ACCOUNT_ID);
    }

    /** Trích giá trị từ map và chuyển sang chuỗi an toàn cho CSV. */
    private static String value(java.util.Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? "" : v.toString();
    }
}
