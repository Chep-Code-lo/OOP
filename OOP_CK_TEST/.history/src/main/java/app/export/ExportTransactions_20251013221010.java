package app.export;

import app.store.DataStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Xuất CSV: Giao dịch (đọc trực tiếp từ DataStore) */
public final class ExportTransactions {
    private ExportTransactions() {}
    public static Path export() throws Exception {
        String[] headers = {"Ngày","Loại","Số tiền","Tài khoản","Danh mục","Ghi chú"
        };
        List<String[]> rows = new ArrayList<>();
        for (var t : DataStore.transactions()) {
            rows.add(new String[]{
                t.date,
                t.type,
                String.valueOf(t.amount),
                t.account,
                t.category,
                t.note == null ? "" : t.note
            });
        }
        return CsvExporter.writeCsv("transactions.csv", headers, rows);
    }
}
