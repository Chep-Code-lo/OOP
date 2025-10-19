package app.account;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nhật ký giao dịch (in-memory).
 * Lưu dữ liệu dưới dạng List<Map<...>> để tiện thao tác bằng Stream API.
 */
public class Ledger {
    /** Nhật ký giao dịch giữ nguyên đối tượng Transaction để tái sử dụng/hiệu chỉnh. */
    private final List<Transaction> entries = new ArrayList<>();

    /** Ghi một giao dịch mới vào sổ. */
    public void record(Transaction txn) {
        if (txn == null) throw new IllegalArgumentException("Giao dịch không được null");
        entries.add(txn);
    }

    public List<Map<String, Object>> query(List<String> accountIds,
                                           Instant start,
                                           Instant end,
                                           List<TxnType> types) {
        // Lọc danh sách bằng Stream để tái sử dụng cho báo cáo/bộ lọc bất kỳ.
        return entries.stream()
                .filter(t -> accountIds == null || accountIds.contains(t.getAccountId()))
                .filter(t -> start == null || !t.getOccurredAt().isBefore(start))
                .filter(t -> end == null || !t.getOccurredAt().isAfter(end))
                .filter(t -> types == null || types.contains(t.getType()))
                .map(Transaction::asMap)
                .collect(Collectors.toList());
    }

    /** Danh sách giao dịch của một tài khoản (snapshot mới để tránh chỉnh sửa ngoài ý muốn). */
    public List<Transaction> listByAccount(String accountId) {
        List<Transaction> filtered = entries.stream()
                .filter(t -> Objects.equals(t.getAccountId(), accountId))
                .collect(Collectors.toList());
        return java.util.Collections.unmodifiableList(filtered);
    }

    /** Tìm giao dịch theo accountId + transactionId. */
    public Optional<Transaction> find(String accountId, String transactionId) {
        return entries.stream()
                .filter(t -> Objects.equals(t.getAccountId(), accountId))
                .filter(t -> Objects.equals(t.getId(), transactionId))
                .findFirst();
    }

    /** Xoá giao dịch khỏi sổ (neu tồn tại). */
    public boolean remove(Transaction txn) {
        return entries.remove(txn);
    }
}
