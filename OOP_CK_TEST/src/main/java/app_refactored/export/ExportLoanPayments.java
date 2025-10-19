package app.export;
import app.store.DataStore;
import java.nio.file.Path;
import java.util.*;
/** Xuất CSV: Lịch sử trả/hoàn nợ (đọc trực tiếp từ DataStore) */
public final class ExportLoanPayments {
    private ExportLoanPayments() {}
    public static Path export() throws Exception {
        String[] headers = {"Mã khoản","Ngày trả","Số tiền","Ghi chú"};
        List<String[]> rows = new ArrayList<>();
        for (var p : DataStore.loanPayments()) {
            rows.add(new String[]{
                value(p, DataStore.LoanPaymentFields.LOAN_ID),
                value(p, DataStore.LoanPaymentFields.DATE),
                value(p, DataStore.LoanPaymentFields.AMOUNT),
                value(p, DataStore.LoanPaymentFields.NOTE)
            });
        }
        return CsvExporter.writeCsv("loan_payments.csv", headers, rows);
    }

    /** Trích giá trị từ map và chuyển sang chuỗi an toàn cho CSV. */
    private static String value(java.util.Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? "" : v.toString();
    }
}
