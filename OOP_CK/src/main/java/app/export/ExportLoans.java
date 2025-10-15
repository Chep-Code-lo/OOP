package app.export;

import app.store.DataStore;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Xuất CSV: Khoản vay / cho vay (đọc trực tiếp từ DataStore) */
public final class ExportLoans {
    private ExportLoans() {}
    public static Path export() throws Exception {
        String[] headers = {
            "ID","Trạng thái","Tên liên hệ","Số tiền","Số điện thoại",
            "Hạn trả","Lãi suất","Ghi chú","Thời điểm tạo"
        };
        List<String[]> rows = new ArrayList<>();
        for (var l : DataStore.loans()) {
            rows.add(new String[]{
                l.loanId,
                l.status == null ? "" : l.status,
                l.name == null ? "" : l.name,
                String.valueOf(l.amount) + " VND",
                l.phone == null ? "" : l.phone,
                l.dueDate == null ? "" : l.dueDate,
                String.valueOf(l.interest),
                l.note == null ? "" : l.note,
                l.createdAt == null ? "" : l.createdAt
            });
        }
        return CsvExporter.writeCsv("loans.csv", headers, rows);
    }
}
