package app.payment;

import app.store.DataStore;
import app.util.ConsoleUtils;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IncomeExpenseReport {

    // Phân loại giao dịch
    public enum TxClass { ALL, INCOME, EXPENSE }

    /** Chạy với bộ lọc: ngày + danh mục + phân loại Thu/Chi */
    public static void run(DateRange range, List<String> categories, TxClass typeFilter) {
        Map<String, Double> byCategory = new LinkedHashMap<>();
        double totalIncome = 0.0;
        double totalExpense = 0.0;

        List<Map<String, Object>> rows = DataStore.transactions();
        for (Map<String, Object> row : rows) {
            String dateStr = String.valueOf(row.get(DataStore.TransactionFields.DATE));
            String cat     = String.valueOf(row.get(DataStore.TransactionFields.CATEGORY));
            String amtStr  = String.valueOf(row.get(DataStore.TransactionFields.AMOUNT));
            String typeStr = row.containsKey(DataStore.TransactionFields.TYPE)
                    ? String.valueOf(row.get(DataStore.TransactionFields.TYPE)) : null;

            if (dateStr == null || dateStr.isBlank()) continue;
            if (cat == null || cat.isBlank()) cat = "(Chưa phân loại)";
            if (amtStr == null || amtStr.isBlank()) amtStr = "0";

            // Lọc theo ngày
            LocalDate d;
            try { d = LocalDate.parse(dateStr); } catch (Exception e) { continue; }
            if (d.isBefore(range.start()) || d.isAfter(range.end())) continue;

            // Lọc theo danh mục (nếu có)
            if (categories != null && !categories.isEmpty() && !categories.contains(cat)) continue;

            // Parse tiền
            double amount;
            try { amount = Double.parseDouble(amtStr); } catch (Exception e) { continue; }

            // Lọc theo phân loại Thu/Chi
            if (typeFilter != null && typeFilter != TxClass.ALL) {
                boolean isIncome;
                if (typeStr != null && !typeStr.isBlank()) {
                    String norm = typeStr.trim().toLowerCase();
                    isIncome = norm.startsWith("in") || norm.startsWith("thu") || norm.equals("+");
                } else {
                    isIncome = amount >= 0; // fallback theo dấu tiền
                }
                if (typeFilter == TxClass.INCOME && !isIncome) continue;
                if (typeFilter == TxClass.EXPENSE &&  isIncome) continue;
            }

            // Cộng dồn
            byCategory.merge(cat, amount, Double::sum);
            if (amount >= 0) totalIncome += amount;
            else totalExpense += -amount;
        }

        double net = totalIncome - totalExpense;
        printTable(byCategory, totalIncome, totalExpense, net);
    }

    private static void printTable(Map<String, Double> byCategory, double totalIncome, double totalExpense, double net) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        ConsoleUtils.printHeader("BÁO CÁO THU - CHI");
        System.out.printf("%-28s | %18s%n", "Danh mục", "Tổng (₫)");
        System.out.println("---------------------------------------------");
        for (var e : byCategory.entrySet()) {
            System.out.printf("%-28s | %18s%n", e.getKey(), nf.format(Math.round(e.getValue())));
        }
        System.out.println("---------------------------------------------");
        System.out.printf("%-28s | %18s%n", "Tổng thu", nf.format(Math.round(totalIncome)));
        System.out.printf("%-28s | %18s%n", "Tổng chi", nf.format(Math.round(totalExpense)));
        System.out.println("---------------------------------------------");
        System.out.printf("%-28s | %18s%n", "Lãi ròng", nf.format(Math.round(net)));
        System.out.println();
    }
}
