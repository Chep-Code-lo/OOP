package app.report;

import app.export.CsvExporter;
import app.model.DateRange;
import java.util.ArrayList;
import java.util.List;

/** Lưu các báo cáo tài chính ra CSV với định dạng thống nhất. */
final class PaymentReportSaver {
    private PaymentReportSaver() {}

    /** Lưu báo cáo Thu - Chi chi tiết (gồm thông tin lọc và tổng hợp). */
    static void saveIncomeExpense(DateRange range,
                                  List<String> categories,
                                  IncomeExpenseReport.TxClass typeFilter,
                                  java.util.Map<String, Double> byCategory,
                                  double totalIncome,
                                  double totalExpense,
                                  double net) {
        String[] headers = quoteAll("Loại", "Tên", "Giá trị", "Ghi chú");
        List<String[]> rows = new ArrayList<>();
        rows.add(quoteAll("#INFO", "Khoảng thời gian", ReportUtils.describeRange(range), ""));
        rows.add(quoteAll("#INFO", "Phân loại", describeType(typeFilter), ""));
        rows.add(quoteAll("#INFO", "Danh mục đã chọn", ReportUtils.describeSelection(categories, "Tất cả danh mục"), ""));
        rows.add(quoteAll("", "", "", ""));
        for (var entry : byCategory.entrySet()) {
            rows.add(quoteAll("Danh mục", entry.getKey(), doubleToString(entry.getValue()), ""));
        }
        rows.add(quoteAll("", "", "", ""));
        rows.add(quoteAll("Tổng", "Thu", doubleToString(totalIncome), ""));
        rows.add(quoteAll("Tổng", "Chi", doubleToString(totalExpense), ""));
        rows.add(quoteAll("Tổng", "Lãi ròng", doubleToString(net), ""));
        writeCsv("report_income_expense.csv", headers, rows, "Thu - Chi");
    }

    /** Lưu báo cáo Khoản vay với thông tin lọc và tổng dư nợ. */
    static void saveLoanReport(DateRange range,
                               List<String> statuses,
                               List<LoanReport.LoanRow> rowsData,
                               double outstanding) {
        String[] headers = quoteAll("Mã", "Tên", "Số tiền", "Hạn trả", "Trạng thái");
        List<String[]> rows = new ArrayList<>();
        rows.add(quoteAll("#INFO", "Khoảng hạn trả", ReportUtils.describeRange(range), "", ReportUtils.describeSelection(statuses, "Tất cả trạng thái")));
        rows.add(quoteAll("", "", "", "", ""));
        for (LoanReport.LoanRow r : rowsData) {
            String dueTxt = r.dueDate() == null ? "-" : r.dueDate().toString();
            rows.add(quoteAll(r.id(), r.name(), doubleToString(r.amount()), dueTxt, safe(r.status())));
        }
        rows.add(quoteAll("", "", "", "", ""));
        rows.add(quoteAll("#TOTAL", "", doubleToString(outstanding), "", ""));
        writeCsv("report_loans.csv", headers, rows, "Khoản vay");
    }

    /** Lưu báo cáo tài khoản kèm tổng số dư. */
    static void saveAccountReport(List<AccountReport.AccountRow> rowsData, double total) {
        String[] headers = quoteAll("ID", "Tên", "Loại", "Số dư", "Ghi chú");
        List<String[]> rows = new ArrayList<>();
        rows.add(quoteAll("#INFO", "Tổng số dư", "", doubleToString(total), ""));
        rows.add(quoteAll("", "", "", "", ""));
        for (AccountReport.AccountRow r : rowsData) {
            rows.add(quoteAll(safe(r.id()), safe(r.name()), safe(r.type()), doubleToString(r.balance()), safe(r.note())));
        }
        writeCsv("report_accounts.csv", headers, rows, "Tài khoản");
    }

    /** Gọi CsvExporter và xử lý ngoại lệ khi ghi file. */
    private static void writeCsv(String fileName, String[] headers, List<String[]> rows, String reportLabel) {
        try {
            CsvExporter.writeCsv(fileName, headers, rows);
        } catch (Exception e) {
            System.err.println("Không thể lưu báo cáo " + reportLabel + " ra CSV: " + e.getMessage());
        }
    }

    /** Bao chuỗi trích dẫn kép để an toàn trong CSV. */
    private static String[] quoteAll(String... values) {
        String[] out = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = quote(values[i]);
        }
        return out;
    }

    /** Escape ký tự " trong một giá trị trước khi ghi CSV. */
    private static String quote(String value) {
        String v = value == null ? "" : value;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    /** Làm tròn số thực đến 2 chữ số thập phân trước khi ghi. */
    private static String doubleToString(double value) {
        return Double.toString(Math.round(value * 100.0) / 100.0);
    }

    /** Chuyển enum phân loại giao dịch thành mô tả tiếng Việt. */
    private static String describeType(IncomeExpenseReport.TxClass typeFilter) {
        if (typeFilter == null) return "Không xác định";
        return switch (typeFilter) {
            case ALL -> "Tất cả";
            case INCOME -> "Thu";
            case EXPENSE -> "Chi";
        };
    }

    /** Tránh NullPointerException khi xử lý chuỗi null. */
    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
