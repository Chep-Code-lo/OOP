package app.account;

import org.example.finance.common.TxnType;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nhật ký giao dịch (in-memory).
 * - Lưu trong RAM, tắt app → mất dữ liệu.
 * - Dùng cho console 1 luồng; không đồng bộ hoá.
 */
public class Ledger {
    /** Danh sách giao dịch theo thứ tự ghi. */
    private final List<Transaction> entries = new ArrayList<>();

    //Ghi 1 giao dịch vào

    public void record(Transaction txn) {
        if (txn == null) throw new IllegalArgumentException("Giao dịch không được null");
        entries.add(txn);
    }

    /** Trả về danh sách chỉ-đọc tất cả giao dịch (không cho sửa từ bên ngoài). */
    public List<Transaction> all() {
        return Collections.unmodifiableList(entries);
    }


    public List<Transaction> query(List<String> accountIds, Instant start, Instant end, List<TxnType> types) {
        return entries.stream()
                .filter(t -> accountIds == null || accountIds.contains(t.getAccountId()))
                .filter(t -> start == null || !t.getOccurredAt().isBefore(start)) // t >= start
                .filter(t -> end   == null || !t.getOccurredAt().isAfter(end))   // t <= end
                .filter(t -> types == null || types.contains(t.getType()))
                .collect(Collectors.toList());
    }
}
