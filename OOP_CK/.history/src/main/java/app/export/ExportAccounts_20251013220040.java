package app.export;

import app.store.DataStore;
import app.export.CsvExporter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Xuất CSV: Tài khoản (đọc trực tiếp từ DataStore) */
public final class ExportAccounts {
    private ExportAccounts() {}
    public static Path export() throws Exception {
        String[] headers = { "MaTaiKhoan", "TenTaiKhoan", "LoaiTaiKhoan", "SoDu", "GhiChu" };
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