package app.account;

import java.math.BigDecimal;
import java.util.*;

/**
 * ReportService – tính tổng THU/CHI/CHÊNH LỆCH  cho
 *  - TẤT CẢ tài khoản, hoặc
 *  - MỘT tài khoản cụ thể.
 * Key kết quả: "income", "expense", "net".
 */
public class ReportService {
    /** Nguồn dữ liệu giao dịch. */
    private final Ledger ledger;

    public ReportService(Ledger ledger) {
        this.ledger = ledger;
    }

    /** Tổng hợp THU/CHI/NET cho TẤT CẢ tài khoản. */
    public Map<String, BigDecimal> summaryAll() {
        return summarize(null);
    }

    /** Tổng hợp THU/CHI/NET cho MỘT tài khoản (theo accountId). */
    public Map<String, BigDecimal> summaryForAccount(String accountId) {
        List<String> ids = (accountId == null || accountId.isEmpty())
                ? null
                : Collections.singletonList(accountId);
        return summarize(ids);
    }

    /** Tổng hợp theo danh sách tài khoản (null = tất cả). */
    public Map<String, BigDecimal> summarize(Collection<String> accountIds) {
        // Lấy tất cả giao dịch; không lọc ngày (start/end = null), không lọc kiểu (type = null)
        List<?> txns = ledger.query(
                (accountIds == null) ? null : new ArrayList<>(accountIds),
                null,  // start
                null,  // end
                null   // type
        );

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Object obj : txns) {

            var t = (org.example.finance.core.Transaction) obj;
            TxnType type = t.getType();
            BigDecimal amount = t.getAmount();

            if (type == TxnType.INCOME || type == TxnType.TRANSFER_IN) {
                income = income.add(amount);
            } else if (type == TxnType.EXPENSE || type == TxnType.TRANSFER_OUT) {
                expense = expense.add(amount);
            }
        }

        BigDecimal net = income.subtract(expense);

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        result.put("income", income);
        result.put("expense", expense);
        result.put("net", net);
        return result;
    }
}
