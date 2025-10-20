package app.export;

import app.repository.DataStore;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Xuất ba báo cáo tài chính tổng hợp:
 *  - Thu/Chi theo chu kỳ (ngày/tháng/năm)
 *  - Danh sách khoản vay/cho vay
 *  - Tình hình từng tài khoản
 */
public final class ExportLoanPayments {
    private ExportLoanPayments() {}

    /** Xuất toàn bộ các báo cáo tài chính tổng hợp và trả về đường dẫn của báo cáo Thu - Chi. */
    public static Path export() throws Exception {
        // Thu/Chi theo từng chu kỳ giúp người dùng nhìn nhanh sự biến động dòng tiền
        Path incomeExpense = exportIncomeExpense();
        // Bổ sung danh sách hợp đồng vay/cho vay để bám sát nghĩa vụ tài chính
        exportLoanSummary();
        // Thêm thống kê từng tài khoản để nắm số dư hiện tại
        exportAccountSummary();
        return incomeExpense;
    }

    /** Xuất báo cáo Thu - Chi theo chu kỳ ngày/tháng/năm dựa vào giao dịch trong DataStore. */
    private static Path exportIncomeExpense() throws Exception {
        String[] headers = {"Chu kỳ", "Khoảng thời gian", "Thu (VND)", "Chi (VND)", "Chênh lệch"};
        Map<PeriodKey, PeriodTotals> byPeriod = new LinkedHashMap<>();

        for (var txn : DataStore.transactions()) {
            // Đổi về LocalDate; nếu dữ liệu lỗi định dạng thì bỏ qua luôn
            LocalDate date = parseDate(value(txn, DataStore.TransactionFields.DATE));
            if (date == null) continue;

            String type = value(txn, DataStore.TransactionFields.TYPE).toUpperCase(Locale.ROOT);
            BigDecimal amount = parseAmount(value(txn, DataStore.TransactionFields.AMOUNT));
            if (amount.signum() <= 0) continue;

            boolean isIncome = type.equals("INCOME") || type.equals("TRANSFER_IN");
            boolean isExpense = type.equals("EXPENSE") || type.equals("TRANSFER_OUT");
            if (!isIncome && !isExpense) continue;

            accumulate(byPeriod, PeriodKey.daily(date), amount, isIncome);
            accumulate(byPeriod, PeriodKey.monthly(date), amount, isIncome);
            accumulate(byPeriod, PeriodKey.yearly(date), amount, isIncome);
        }

        List<String[]> rows = new ArrayList<>();
        for (var entry : byPeriod.entrySet()) {
            PeriodTotals totals = entry.getValue();
            rows.add(new String[]{
                    entry.getKey().label(),
                    entry.getKey().bucket(),
                    totals.income.toPlainString(),
                    totals.expense.toPlainString(),
                    totals.net().toPlainString()
            });
        }

        return CsvExporter.writeCsv("report_income_expense.csv", headers, rows);
    }

    /** Xuất danh sách hợp đồng vay/cho vay đang lưu trong DataStore. */
    private static void exportLoanSummary() throws Exception {
        String[] headers = {
                "Mã hợp đồng", "Trạng thái", "Tên", "Số tiền (VND)",
                "Ngày vay", "Ngày trả", "Lãi suất", "Loại lãi", "Ghi chú"
        };
        List<String[]> rows = new ArrayList<>();
        for (var loan : DataStore.loans()) {
            rows.add(new String[]{
                    value(loan, DataStore.LoanFields.ID),
                    value(loan, DataStore.LoanFields.STATUS),
                    value(loan, DataStore.LoanFields.NAME),
                    value(loan, DataStore.LoanFields.AMOUNT),
                    value(loan, DataStore.LoanFields.BORROW_DATE),
                    value(loan, DataStore.LoanFields.DUE_DATE),
                    value(loan, DataStore.LoanFields.INTEREST),
                    value(loan, DataStore.LoanFields.TYPE),
                    value(loan, DataStore.LoanFields.NOTE)
            });
        }
        CsvExporter.writeCsv("report_loans.csv", headers, rows);
    }

    /** Xuất tổng quan từng tài khoản hiện có để theo dõi số dư. */
    private static void exportAccountSummary() throws Exception {
        String[] headers = {"ID", "Tên", "Loại", "Số dư (VND)", "Ghi chú"};
        List<String[]> rows = new ArrayList<>();
        for (var account : DataStore.accounts()) {
            rows.add(new String[]{
                    value(account, DataStore.AccountFields.ID),
                    value(account, DataStore.AccountFields.NAME),
                    value(account, DataStore.AccountFields.TYPE),
                    value(account, DataStore.AccountFields.BALANCE),
                    value(account, DataStore.AccountFields.NOTE)
            });
        }
        CsvExporter.writeCsv("report_accounts.csv", headers, rows);
    }

    /** Cộng dồn số liệu thu/chi vào nhóm chu kỳ tương ứng. */
    private static void accumulate(Map<PeriodKey, PeriodTotals> map,
                                   PeriodKey key,
                                   BigDecimal amount,
                                   boolean income) {
        PeriodTotals totals = map.computeIfAbsent(key, k -> new PeriodTotals());
        if (income) {
            totals.income = totals.income.add(amount);
        } else {
            totals.expense = totals.expense.add(amount);
        }
    }

    /** Lấy giá trị chuỗi an toàn từ bản ghi Map. */
    private static String value(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? "" : v.toString();
    }

    /** Chuyển đổi chuỗi số tiền sang BigDecimal, lỗi định dạng -> 0. */
    private static BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ignore) {
            return BigDecimal.ZERO;
        }
    }

    /** Chuyển đổi chuỗi ngày sang LocalDate, lỗi định dạng -> null. */
    private static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException ignore) {
            return null;
        }
    }

    private record PeriodKey(String label, String bucket) {
        /** Tạo khoá nhóm theo ngày. */
        static PeriodKey daily(LocalDate date) {
            return new PeriodKey("Ngày", date.toString());
        }
        /** Tạo khoá nhóm theo tháng (YYYY-MM). */
        static PeriodKey monthly(LocalDate date) {
            return new PeriodKey("Tháng", date.getYear() + "-" + String.format("%02d", date.getMonthValue()));
        }
        /** Tạo khoá nhóm theo năm. */
        static PeriodKey yearly(LocalDate date) {
            return new PeriodKey("Năm", Integer.toString(date.getYear()));
        }
    }

    private static final class PeriodTotals {
        private BigDecimal income = BigDecimal.ZERO;
        private BigDecimal expense = BigDecimal.ZERO;
        /** Tính chênh lệch thu - chi. */
        private BigDecimal net() {
            return income.subtract(expense);
        }
    }
}
