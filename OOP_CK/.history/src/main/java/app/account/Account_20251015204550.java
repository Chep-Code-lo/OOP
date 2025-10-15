package app.account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * Account (abstract) – một tài khoản.
 * Invariants: name != blank, balance scale = 2.
 * Dữ liệu nền được lưu bằng Map để tiện thao tác với Stream API.
 */
public abstract class Account {
    protected static final String KEY_ID = "id";
    protected static final String KEY_NAME = "name";
    protected static final String KEY_TYPE = "type";
    protected static final String KEY_BALANCE = "balance";
    protected static final String KEY_CREATED_AT = "createdAt";
    protected static final String KEY_UPDATED_AT = "updatedAt";

    /** Map lưu trữ trạng thái nội bộ; vừa phục vụ nghiệp vụ vừa cung cấp cho DataStore. */
    private final Map<String, Object> data;

    /** Tạo tài khoản với tên và loại, lưu dữ liệu trong Map. */
    protected Account(String name, String type) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Tên tài khoản không được để trống");
        data = new LinkedHashMap<>();
        data.put(KEY_ID, shortId());
        data.put(KEY_NAME, name.trim());
        data.put(KEY_TYPE, type);
        data.put(KEY_BALANCE, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        Instant now = Instant.now();
        data.put(KEY_CREATED_AT, now);
        data.put(KEY_UPDATED_AT, now);
    }

    protected Map<String, Object> data() { return data; }

    public String getId() { return (String) data.get(KEY_ID); }
    public String getName() { return (String) data.get(KEY_NAME); }
    public String getType() { return (String) data.get(KEY_TYPE); }
    public BigDecimal getBalance() { return (BigDecimal) data.get(KEY_BALANCE); }
    public Instant getCreatedAt() { return (Instant) data.get(KEY_CREATED_AT); }
    public Instant getUpdatedAt() { return (Instant) data.get(KEY_UPDATED_AT); }

    /** Map đại diện cho tài khoản (view bất biến dùng chung với DataStore/Stream). */
    public Map<String, Object> asMap() { return java.util.Collections.unmodifiableMap(data); }

    /** Đổi tên (không chấp nhận rỗng). */
    public void rename(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("Tên tài khoản không được để trống");
        data.put(KEY_NAME, newName.trim());
        data.put(KEY_UPDATED_AT, Instant.now());
    }

    /** Nạp tiền: amount==0 bỏ qua; <0 → lỗi. */
    public void deposit(BigDecimal amount, Instant when) {
        if (amount == null) throw new IllegalArgumentException("Thiếu số tiền");
        if (amount.signum() == 0) return; // bỏ qua 0
        if (amount.signum() < 0) throw new IllegalArgumentException("Số tiền không được âm");
        BigDecimal v = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal newBalance = getBalance().add(v);
        data.put(KEY_BALANCE, newBalance);
        data.put(KEY_UPDATED_AT, (when == null) ? Instant.now() : when);
    }

    /** Rút tiền: amount==0 bỏ qua; thiếu số dư → lỗi; <0 → lỗi. */
    public void withdraw(BigDecimal amount, Instant when) {
        if (amount == null) throw new IllegalArgumentException("Thiếu số tiền");
        if (amount.signum() == 0) return; // bỏ qua 0
        if (amount.signum() < 0) throw new IllegalArgumentException("Số tiền không được âm");
        BigDecimal v = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = getBalance();
        if (balance.compareTo(v) < 0)
            throw new IllegalStateException("Số dư không đủ");
        data.put(KEY_BALANCE, balance.subtract(v));
        data.put(KEY_UPDATED_AT, (when == null) ? Instant.now() : when);
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
