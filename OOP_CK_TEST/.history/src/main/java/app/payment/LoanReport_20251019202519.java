package app.payment;

import app.store.DataStore;
import app.ui.ConsoleUtils;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LoanReport {

    record LoanRow(String id, String name, double amount, LocalDate dueDate, String status) {}

    /** Chạy với bộ lọc theo hạn trả (due date) + trạng thái */
    public static void run(DateRange range, List<String> statuses) {
        List<LoanRow> rows = new ArrayList<>();
        List<Map<String, Object>> raw = DataStore.loans();

        // Chuẩn hoá danh sách trạng thái lựa chọn (so sánh không phân biệt hoa/thường)
        java.util.Set<String> statusSet = new java.util.HashSet<>();
        if (statuses != null) {
            for (String s : statuses) if (s != null) statusSet.add(s.trim().toLowerCase());
        }

        for (Map<String, Object> r : raw) {
            String id       = String.valueOf(r.get(DataStore.LoanFields.ID));
            String name     = String.valueOf(r.get(DataStore.LoanFields.NAME));
            String amountStr= String.valueOf(r.get(DataStore.LoanFields.AMOUNT));
            String dueStr   = String.valueOf(r.get(DataStore.LoanFields.DUE_DATE));
            String status   = String.valueOf(r.get(DataStore.LoanFields.STATUS));

            if (name == null || name.isBlank()) name = "(Không tên)";
            double amount;
            try { amount = Double.parseDouble(amountStr); } catch (Exception e) { amount = 0.0; }

            LocalDate due = null;
            try { if (dueStr != null && !dueStr.isBlank()) due = LocalDate.parse(dueStr); } catch (Exception ignore) {}

            // Lọc theo hạn trả (nếu có due)
            if (due != null && (due.isBefore(range.start()) || due.isAfter(range.end()))) {
                continue;
            }

            // Lọc theo trạng thái (nếu người dùng có chọn)
            if (!statusSet.isEmpty()) {
                String norm = status == null ? "" : status.trim().toLowerCase();
                if (!statusSet.contains(norm)) continue;
            }

            rows.add(new LoanRow(id, name, amount, due, status));
        }

        rows.sort(Comparator.comparing((LoanRow r) -> r.dueDate == null ? LocalDate.MAX : r.dueDate));
        double outstanding = rows.stream().mapToDouble(LoanRow::amount).sum();
        printTable(rows, outstanding);
    }

    private static void printTable(List<LoanRow> rows, double outstanding) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ConsoleUtils.printHeader("KHOẢN VAY");
        System.out.printf("%-8s | %-20s | %14s | %-12s | %-12s%n", "Mã", "Tên", "Số tiền (₫)", "Hạn trả", "Trạng thái");
        System.out.println("--------------------------------------------------------------------");
        for (LoanRow r : rows) {
            String dueTxt = r.dueDate == null ? "-" : r.dueDate.toString();
            System.out.printf("%-8s | %-20s | %14s | %-12s | %-12s%n",
                    safe(r.id), truncate(r.name, 20), nf.format(Math.round(r.amount)), dueTxt, truncate(safe(r.status), 12));
        }
        System.out.println("--------------------------------------------------------------------");
        System.out.printf("%-31s   %14s%n", "Tổng dư nợ:", nf.format(Math.round(outstanding)));
        System.out.println();
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max-1) + "…";
    }
}
