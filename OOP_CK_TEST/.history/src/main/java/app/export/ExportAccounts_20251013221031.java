package app.export;

import app.store.DataStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Xuất CSV: Tài khoản (đọc trực tiếp từ DataStore) */
public final class ExportAccounts {
    private ExportAccounts() {}
    public static Path export() throws Exception {
        String[] headers = {"Mã tài khoản","Tên tài khoản","Loại tài khoản", "Số dư","Ghi chú"
        };
        List<String[]> rows = new ArrayList<>();
        for (var a : DataStore.accounts()) {
            rows.add(new String[]{
                a.id == null ? "" : a.id,
                a.name,
                a.type,
                String.valueOf(a.balance),
                a.note == null ? "" : a.note
            });
        }
        return CsvExporter.writeCsv("accounts.csv", headers, rows);
    }
}
