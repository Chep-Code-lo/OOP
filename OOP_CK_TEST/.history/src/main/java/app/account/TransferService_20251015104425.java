package app.account;


import java.time.Instant;
import java.util.Map;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Quy ước:
 * - Ghi 2 dòng giao dịch vào sổ (Ledger):
 *   + TK nguồn  : TRANSFER_OUT (tiền ra)
 *   + TK đích   : TRANSFER_IN  (tiền vào)
 * Trường hợp đặc biệt khi thực thi:
 * - Thiếu số dư  : ném IllegalStateException("Số dư không đủ") từ Account.withdraw().
 */
public class TransferService {
    /** Sổ giao dịch (in-memory). */
    private final Ledger ledger;
    /** Danh mục tài khoản hiện có, tra cứu theo id. */
    private final Map<String, Account> accounts;

    public TransferService(Ledger ledger, Map<String, Account> accounts) {
        this.ledger = ledger;
        this.accounts = accounts;
    }

    //Chuyển khoản src -> dst.

    public void transfer(String srcId, String dstId, BigDecimal amount, Instant when, String note) {
        // --- 1) Kiểm tra tham số bắt buộc
        if (srcId == null || srcId.isBlank()) throw new IllegalArgumentException("Thiếu mã tài khoản nguồn");
        if (dstId == null || dstId.isBlank()) throw new IllegalArgumentException("Thiếu mã tài khoản đích");
        if (srcId.equals(dstId)) throw new IllegalArgumentException("Tài khoản nguồn và đích phải khác nhau");

        // --- 2) Tra cứu tài khoản
        Account src = accounts.get(srcId);
        Account dst = accounts.get(dstId);
        if (src == null) throw new IllegalArgumentException("Không tìm thấy tài khoản nguồn");
        if (dst == null) throw new IllegalArgumentException("Không tìm thấy tài khoản đích");

        // --- 3) Kiểm tra số tiền & tiền tệ
        if (amount == null) throw new IllegalArgumentException("Thiếu thông tin số tiền chuyển");
        if (amount.signum() <= 0) throw new IllegalArgumentException("Số tiền chuyển phải > 0");

        BigDecimal amt = amount.setScale(2, RoundingMode.HALF_UP);

        // --- 4) Xác định thời điểm ghi nhận (null -> now)
        Instant ts = (when == null) ? Instant.now() : when;

        // --- 5) Thực hiện rút ở nguồn (có thể ném IllegalStateException nếu không đủ số dư)
        src.withdraw(amt, ts);
        //     Ghi dòng TRANSFER_OUT cho tài khoản nguồn
        ledger.record(Transaction.builder()
                .accountId(src.getId())
                .type(TxnType.TRANSFER_OUT)
                .amount(amt)
                .note(note)
                .counterpartyAccountId(dst.getId()).build());

        // --- 6) Thực hiện nạp ở đích
        dst.deposit(amt, ts);
        //     Ghi dòng TRANSFER_IN cho tài khoản đích
        ledger.record(Transaction.builder()
                .accountId(dst.getId())
                .type(TxnType.TRANSFER_IN)
                .amount(amt)
                .note(note)
                .counterpartyAccountId(src.getId()).build());
    }

}
