package org.example.finance.core;

import org.example.finance.common.TxnType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Transaction – BẢN GHI GIAO DỊCH (bất biến).
 *  - Gắn với 1 tài khoản (accountId) và 1 loại giao dịch (TxnType).
 *  - Được tạo qua Builder; hợp lệ thì mới build được.
 */
public final class Transaction {
    private final String id;
    private final String accountId;
    private final TxnType type;
    private final BigDecimal amount;
    private final Instant occurredAt;
    private final String note;
    private final String counterpartyAccountId;

    private Transaction(Builder b) {
        if (b.accountId == null || b.accountId.isBlank())
            throw new IllegalArgumentException("Thiếu mã tài khoản cho giao dịch");
        if (b.type == null)
            throw new IllegalArgumentException("Thiếu loại giao dịch");
        if (b.amount == null)
            throw new IllegalArgumentException("Thiếu số tiền giao dịch");

        this.id = b.id;
        this.accountId = b.accountId;
        this.type = b.type;
        this.amount = b.amount.setScale(2, RoundingMode.HALF_UP);
        this.occurredAt = (b.occurredAt == null) ? Instant.now() : b.occurredAt;
        this.note = (b.note == null) ? "" : b.note;
        this.counterpartyAccountId = b.counterpartyAccountId;
    }

    // Getters
    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public TxnType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getNote() { return note; }
    public String getCounterpartyAccountId() { return counterpartyAccountId; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id = java.util.UUID.randomUUID().toString();
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
