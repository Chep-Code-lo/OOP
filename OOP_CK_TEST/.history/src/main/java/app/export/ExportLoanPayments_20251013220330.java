package app.export;

import app.store.DataStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Xuất CSV: Lịch sử trả/hoàn nợ (đọc trực tiếp từ DataStore) */
public final class ExportLoanPayments {
    private ExportLoanPayments() {}
    public static Path export() throws Exception {
        String[] headers = { "MaKhoan", "NgayTra", "SoTien", "GhiChu" };
        List<String[]> rows = new ArrayList<>();
        for (var p : DataStore.loanPayments()) {
            rows.add(new String[]{
                p.loanId,
                p.date,
                String.valueOf(p.amount),
                p.note == null ? "" : p.note
            });
        }
        return CsvExporter.writeCsv("loan_payments.csv", headers, rows);
    }
}