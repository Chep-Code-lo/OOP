package app.export;
import app.store.DataStore;
import java.nio.file.Path;
import java.util.*;

/** Xuất CSV: Tài khoản (đọc trực tiếp từ DataStore) */
public final class ExportAccounts {
    private ExportAccounts() {}
    public static Path export() throws Exception {
        String[] headers = {"Mã tài khoản","Tên tài khoản","Loại tài khoản", "Số dư","Ghi chú"};
        List<String[]> rows = new ArrayList<>();
        for (var a : DataStore.accounts()) {
            rows.add(new String[]{
                value(a, DataStore.AccountFields.ID),
                value(a, DataStore.AccountFields.NAME),
                value(a, DataStore.AccountFields.TYPE),
                value(a, DataStore.AccountFields.BALANCE),
                value(a, DataStore.AccountFields.NOTE)
            });
        }
        return CsvExporter.writeCsv("accounts.csv", headers, rows);
    }

    /** Trích giá trị từ map và chuyển sang chuỗi an toàn cho CSV. */
    private static String value(java.util.Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? "" : v.toString();
    }
}
