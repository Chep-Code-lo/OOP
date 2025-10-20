package app.report;
import app.repository.DataStore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Báo cáo tổng quan tài khoản: in bảng và lưu file hỗ trợ PaymentReportSaver. */
public class AccountReport {

    record AccountRow(String id, String name, String type, double balance, String note) {}

    /** Thu thập dữ liệu tài khoản, sắp xếp và xuất báo cáo. */
    public static void run() {
        List<AccountRow> rows = new ArrayList<>();
        for (Map<String, Object> r : DataStore.accounts()) {
            String id = String.valueOf(r.get(DataStore.AccountFields.ID));
            String name = String.valueOf(r.get(DataStore.AccountFields.NAME));
            String type = String.valueOf(r.get(DataStore.AccountFields.TYPE));
            String balStr = String.valueOf(r.get(DataStore.AccountFields.BALANCE));
            String note = String.valueOf(r.get(DataStore.AccountFields.NOTE));

            if (name == null || name.isBlank()) name = "(Unnamed)";
            double bal;
            try { bal = Double.parseDouble(balStr); } catch (Exception e) { bal = 0.0; }

            rows.add(new AccountRow(id, name, type, bal, note));
        }
        rows.sort(Comparator.comparing(AccountRow::name, String.CASE_INSENSITIVE_ORDER));

        double total = rows.stream().mapToDouble(AccountRow::balance).sum();
        PaymentReportSaver.saveAccountReport(rows, total);
        printTable(rows, total);
    }

    /** In bảng báo cáo ra console theo định dạng cột cố định. */
    private static void printTable(List<AccountRow> rows, double total) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        System.out.println("=============== ACCOUNTS ===============");
        System.out.printf("%-8s | %-20s | %-10s | %14s | %-20s%n", "ID", "Name", "Type", "Balance (₫)", "Note");
        System.out.println("----------------------------------------------------------------------------");
        for (AccountRow r : rows) {
            System.out.printf("%-8s | %-20s | %-10s | %14s | %-20s%n",
                    safe(r.id), truncate(r.name, 20), truncate(safe(r.type), 10),
                    nf.format(Math.round(r.balance)), truncate(safe(r.note), 20));
        }
        System.out.println("----------------------------------------------------------------------------");
        System.out.printf("%-43s %14s%n", "Total Balance:", nf.format(Math.round(total)));
        System.out.println();
    }

    /** Trả về chuỗi rỗng nếu null. */
    private static String safe(String s) { return s == null ? "" : s; }
    /** Cắt ngắn chuỗi nếu vượt quá độ dài mong muốn. */
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max-1) + "…";
    }
}
