package app.service;
import java.math.*;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Function;

/**
 * Xử lý chuyển khoản giữa các tài khoản (dữ liệu được truy vấn qua Stream).
 * Ghi 2 dòng giao dịch vào sổ:
 *  - Nguồn: TRANSFER_OUT
 *  - Đích : TRANSFER_IN
 */
public class TransferService {
    private final Ledger ledger;
    /** Callback để lấy Account theo id (tránh lệ thuộc cấu trúc lưu trữ cụ thể). */
    private final Function<String, Account> accountResolver;

    protected TransferService(Ledger ledger, Function<String, Account> accountResolver) {
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.accountResolver = Objects.requireNonNull(accountResolver, "accountResolver");
    }

    public void transfer(String srcId, String dstId, BigDecimal amount, Instant when, String note) {
        if (srcId == null || srcId.isBlank()) throw new IllegalArgumentException("Thiếu mã tài khoản nguồn");
        if (dstId == null || dstId.isBlank()) throw new IllegalArgumentException("Thiếu mã tài khoản đích");
        if (srcId.equals(dstId)) throw new IllegalArgumentException("Tài khoản nguồn và đích phải khác nhau");
        if (amount == null) throw new IllegalArgumentException("Thiếu thông tin số tiền chuyển");
        if (amount.signum() <= 0) throw new IllegalArgumentException("Số tiền chuyển phải > 0");

        Account src = accountResolver.apply(srcId);
        Account dst = accountResolver.apply(dstId);
        if (src == null) throw new IllegalArgumentException("Không tìm thấy tài khoản nguồn");
        if (dst == null) throw new IllegalArgumentException("Không tìm thấy tài khoản đích");

        BigDecimal amt = amount.setScale(2, RoundingMode.HALF_UP);
        Instant ts = (when == null) ? Instant.now() : when;

        src.withdraw(amt, ts); // Giảm số dư ở nguồn
        ledger.record(Transaction.builder()
                .accountId(src.getId())
                .type(TxnType.TRANSFER_OUT)
                .amount(amt)
                .occurredAt(ts)
                .note(note)
                .counterpartyAccountId(dst.getId())
                .build());

        dst.deposit(amt, ts); // Tăng số dư ở đích
        ledger.record(Transaction.builder()
                .accountId(dst.getId())
                .type(TxnType.TRANSFER_IN)
                .amount(amt)
                .occurredAt(ts)
                .note(note)
                .counterpartyAccountId(src.getId())
                .build());
    }
}
