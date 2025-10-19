package app.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        data.put(KEY_AMOUNT, b.amount.setScale(2, RoundingMode.HALF_UP));
        data.put(KEY_OCCURRED_AT, (b.occurredAt == null) ? Instant.now() : b.occurredAt);
        data.put(KEY_NOTE, (b.note == null) ? "" : b.note);
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
    public String getCounterpartyAccountId() { return (String) data.get(KEY_COUNTERPARTY_ID); }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id = UUID.randomUUID().toString();
        private String accountId;
        private TxnType type;
        private BigDecimal amount;
        private Instant occurredAt;
        private String note;
        private String counterpartyAccountId;

        public Builder id(String v) { this.id = v; return this; }
        public Builder accountId(String v){ this.accountId = v; return this; }
        public Builder type(TxnType v){ this.type = v; return this; }
        public Builder amount(BigDecimal v){ this.amount = v; return this; }
        public Builder occurredAt(Instant v){ this.occurredAt = v; return this; }
        public Builder note(String v){ this.note = v; return this; }
        public Builder counterpartyAccountId(String v){ this.counterpartyAccountId = v; return this; }
        public Transaction build(){ return new Transaction(this); }
    }
}
