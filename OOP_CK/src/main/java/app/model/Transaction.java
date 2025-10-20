package app.model;
import java.math.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.*;

/**
 * Transaction – BẢN GHI GIAO DỊCH.
 *  - Dữ liệu được lưu trong Map để dễ thao tác bằng Stream API.
 */
public final class Transaction {
    public static final String KEY_ID = "id";
    public static final String KEY_ACCOUNT_ID = "accountId";
    public static final String KEY_TYPE = "type";
    public static final String KEY_AMOUNT = "amount";
    public static final String KEY_OCCURRED_AT = "occurredAt";
    public static final String KEY_NOTE = "note";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_COUNTERPARTY_ID = "counterpartyAccountId";

    private final Map<String, Object> data;

    private Transaction(Builder b) {
        if (b.accountId == null || b.accountId.isBlank())
            throw new IllegalArgumentException("Thiếu mã tài khoản cho giao dịch");
        if (b.type == null)
            throw new IllegalArgumentException("Thiếu loại giao dịch");
        if (b.amount == null)
            throw new IllegalArgumentException("Thiếu số tiền giao dịch");

        data = new LinkedHashMap<>();
        data.put(KEY_ID, b.id);
        data.put(KEY_ACCOUNT_ID, b.accountId);
        data.put(KEY_TYPE, b.type);
        data.put(KEY_AMOUNT, normalizeAmount(b.amount));
        data.put(KEY_OCCURRED_AT, (b.occurredAt == null) ? Instant.now() : b.occurredAt);
        data.put(KEY_NOTE, sanitize(b.note));
        data.put(KEY_CATEGORY, sanitize(b.category));
        data.put(KEY_COUNTERPARTY_ID, b.counterpartyAccountId);
    }

    /** View bất biến phục vụ Ledger/DataStore mà vẫn phản ánh thay đổi của đối tượng. */
    public Map<String, Object> asMap() { return java.util.Collections.unmodifiableMap(data); }

    // Convenience getters
    public String getId() { return (String) data.get(KEY_ID); }
    public String getAccountId() { return (String) data.get(KEY_ACCOUNT_ID); }
    public TxnType getType() { return (TxnType) data.get(KEY_TYPE); }
    public BigDecimal getAmount() { return (BigDecimal) data.get(KEY_AMOUNT); }
    public Instant getOccurredAt() { return (Instant) data.get(KEY_OCCURRED_AT); }
    public String getNote() { return (String) data.get(KEY_NOTE); }
    public String getCategory() { return (String) data.get(KEY_CATEGORY); }
    public String getCounterpartyAccountId() { return (String) data.get(KEY_COUNTERPARTY_ID); }

    /** Cập nhật loại giao dịch (thu/chi/chuyển khoản). */
    public void updateType(TxnType type) {
        if (type == null) throw new IllegalArgumentException("Thiếu loại giao dịch");
        data.put(KEY_TYPE, type);
    }

    /** Cập nhật số tiền giao dịch và chuẩn hoá về 2 chữ số thập phân. */
    public void updateAmount(BigDecimal amount) {
        data.put(KEY_AMOUNT, normalizeAmount(amount));
    }

    /** Cập nhật thời điểm phát sinh giao dịch. */
    public void updateOccurredAt(Instant occurredAt) {
        data.put(KEY_OCCURRED_AT, occurredAt == null ? Instant.now() : occurredAt);
    }

    /** Ghi chú giao dịch (cắt khoảng trắng dư). */
    public void updateNote(String note) {
        data.put(KEY_NOTE, sanitize(note));
    }

    /** Gán danh mục cho giao dịch. */
    public void updateCategory(String category) {
        data.put(KEY_CATEGORY, sanitize(category));
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id = UUID.randomUUID().toString();
        private String accountId;
        private TxnType type;
        private BigDecimal amount;
        private Instant occurredAt;
        private String note;
        private String counterpartyAccountId;
        private String category;

        public Builder id(String v) { this.id = v; return this; }
        public Builder accountId(String v){ this.accountId = v; return this; }
        public Builder type(TxnType v){ this.type = v; return this; }
        public Builder amount(BigDecimal v){ this.amount = v; return this; }
        public Builder occurredAt(Instant v){ this.occurredAt = v; return this; }
        public Builder note(String v){ this.note = v; return this; }
        public Builder counterpartyAccountId(String v){ this.counterpartyAccountId = v; return this; }
        public Builder category(String v){ this.category = v; return this; }
        public Transaction build(){ return new Transaction(this); }
    }

    private static String sanitize(String value) {
        return (value == null) ? "" : value.trim();
    }

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Thiếu số tiền giao dịch");
        if (amount.signum() <= 0) throw new IllegalArgumentException("Số tiền phải > 0");
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
