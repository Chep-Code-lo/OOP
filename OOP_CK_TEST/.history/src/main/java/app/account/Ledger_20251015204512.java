package app.account;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nhật ký giao dịch (in-memory).
 * Lưu dữ liệu dưới dạng List<Map<...>> để tiện thao tác bằng Stream API.
 */
public class Ledger {
    /** Danh sách giao dịch dưới dạng Map bất biến (phù hợp cho Stream API). */
    private final List<Map<String, Object>> entries = new ArrayList<>();

    /** Ghi một giao dịch mới vào sổ (map bất biến lấy từ Transaction). */
    public void record(Transaction txn) {
        if (txn == null) throw new IllegalArgumentException("Giao dịch không được null");
        entries.add(txn.asMap());
    }

    public List<Map<String, Object>> query(List<String> accountIds,
                                           Instant start,
                                           Instant end,
                                           List<TxnType> types) {
        // Lọc danh sách bằng Stream để tái sử dụng cho báo cáo/bộ lọc bất kỳ.
        return entries.stream()
                .filter(t -> accountIds == null || accountIds.contains((String) t.get(Transaction.KEY_ACCOUNT_ID)))
                .filter(t -> {
                    Instant occurredAt = (Instant) t.get(Transaction.KEY_OCCURRED_AT);
                    return start == null || !occurredAt.isBefore(start);
                })
                .filter(t -> {
                    Instant occurredAt = (Instant) t.get(Transaction.KEY_OCCURRED_AT);
                    return end == null || !occurredAt.isAfter(end);
                })
                .filter(t -> types == null || types.contains((TxnType) t.get(Transaction.KEY_TYPE)))
                .collect(Collectors.toList());
    }
}
