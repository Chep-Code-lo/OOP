package org.example.finance.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Account (abstract) – một tài khoản.
 * Invariants: name != blank, balance scale = 2.
 * Có thể ném lỗi: tên rỗng; rút khi không đủ tiền.
 */
public abstract class Account {
    private final String id; // dùng shortId() trong constructor
    private String name;
    private BigDecimal balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private Instant createdAt = Instant.now();
    private Instant updatedAt = createdAt;

    /** Tạo tài khoản với tên */
    protected Account(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Tên tài khoản không được để trống");
        this.name = name.trim();
        this.id = shortId(); // <-- ID ngắn 8 ký tự
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }


    /** Đổi tên (không chấp nhận rỗng). */
    public void rename(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Tên tài khoản không được để trống");
        this.name = newName.trim();
        this.updatedAt = Instant.now();
    }

    /** Nạp tiền: amount==0 bỏ qua; <0 → lỗi. */
    public void deposit(BigDecimal amount, Instant when) {
        if (amount == null) throw new IllegalArgumentException("Thiếu số tiền");
        if (amount.signum() == 0) return; // bỏ qua 0
        if (amount.signum() < 0) throw new IllegalArgumentException("Số tiền không được âm");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        balance = balance.add(amount);
        updatedAt = (when == null) ? Instant.now() : when;
    }

    /** Rút tiền: amount==0 bỏ qua; thiếu số dư → lỗi; <0 → lỗi. */
    public void withdraw(BigDecimal amount, Instant when) {
        if (amount == null) throw new IllegalArgumentException("Thiếu số tiền");
        if (amount.signum() == 0) return; // bỏ qua 0
        if (amount.signum() < 0) throw new IllegalArgumentException("Số tiền không được âm");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (balance.compareTo(amount) < 0)
            throw new IllegalStateException("Số dư không đủ");
        balance = balance.subtract(amount);
        updatedAt = (when == null) ? Instant.now() : when;
    }

    /** Tạo ID ngắn: UUID -> bỏ '-' -> lấy 8 ký tự đầu -> in hoa. */
    private static String shortId() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}
