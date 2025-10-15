package app.account;

/** Loại giao dịch . */
public enum TxnType {
    INCOME,        // THU nhập (tiền vào)
    EXPENSE,       // CHI tiêu  (tiền ra)
    TRANSFER_OUT,  // Ghi ở tài khoản NGUỒN khi chuyển nội bộ (tiền ra)
    TRANSFER_IN    // Ghi ở tài khoản ĐÍCH  khi chuyển nội bộ (tiền vào)
}
