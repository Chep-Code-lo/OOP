package app.export;

import app.store.DataStore;
import app.export.CsvExporter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Xuất CSV: Khoản vay / cho vay (đọc trực tiếp từ DataStore) */
public final class ExportLoans {
    private ExportLoans() {}
    public static Path export() throws Exception {
        String[] headers = {
            "MaKhoan", "Loai", "NguoiLienQuan", "SoTien", "NgayBatDau", "NgayDenHan",
            "LaiSuat", "KieuLai", "TrangThai", "GhiChu"
        };
        List<String[]> rows = new ArrayList<>();
        for (var l : DataStore.loans()) {
            rows.add(new String[]{
                l.loanId,
                l.kind,
                l.person,
                String.valueOf(l.amount),
                l.start,
                l.due,
                l.rate,
                l.interestKind,
                l.status,
                l.note == null ? "" : l.note
            });
        }
        return CsvExporter.writeCsv("loans.csv", headers, rows);
    }
}