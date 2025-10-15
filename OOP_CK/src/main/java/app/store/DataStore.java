package app.store;

import java.util.ArrayList;
import java.util.List;

/**
 * Bộ nhớ tạm (in‑memory) để gom dữ liệu trong các menu nghiệp vụ.
 * Khi người dùng chọn "Lưu ra CSV", các Exporter sẽ đọc từ đây và ghi ra file.
 */
public final class DataStore {
    private DataStore() {}

    // ======= Models tối giản (có thể thay bằng model hiện có của bạn) =======
    public static class Account {
        public String id;        // MaTaiKhoan (tuỳ chọn)
        public String name;      // TenTaiKhoan
        public String type;      // LoaiTaiKhoan: NganHang/ViDienTu/TienMat/Khac
        public long balance;     // SoDu
        public String note;      // GhiChu
        public Account(String id, String name, String type, long balance, String note) {
            this.id = id; this.name = name; this.type = type; this.balance = balance; this.note = note;
        }
    }

    public static class Transaction {
        public String date;      // Ngay (YYYY-MM-DD)
        public String type;      // Loai: ThuNhap | ChiTieu
        public long amount;      // SoTien
        public String account;   // TaiKhoan
        public String category;  // DanhMuc
        public String note;      // GhiChu
        public Transaction(String date, String type, long amount, String account, String category, String note) {
            this.date=date; this.type=type; this.amount=amount; this.account=account; this.category=category; this.note=note;
        }
    }

    public static class Loan {
        public String loanId;    // id
        public String status;    // CoNo | ChNo
        public String name;      // TenNguoiLienHe
        public double amount;    // SoTien
        public String phone;     // SoDienThoai
        public String dueDate;   // HanTra
        public double interest;  // LaiSuat (%)
        public String note;      // GhiChu
        public String createdAt; // ThoiGianTao
        public Loan(String loanId, String status, String name, double amount, String phone,
                    String dueDate, double interest, String note, String createdAt) {
            this.loanId = loanId;
            this.status = status;
            this.name = name;
            this.amount = amount;
            this.phone = phone;
            this.dueDate = dueDate;
            this.interest = interest;
            this.note = note;
            this.createdAt = createdAt;
        }
    }

    public static class LoanPayment {
        public String loanId;   // link tới Loan.MaKhoan
        public String date;     // NgayTra (hoặc NgayHoan)
        public long amount;     // SoTien
        public String note;     // GhiChu
        public LoanPayment(String loanId, String date, long amount, String note) {
            this.loanId=loanId; this.date=date; this.amount=amount; this.note=note;
        }
    }

    // ======= Bộ đệm =======
    private static final List<Account> ACCOUNTS = new ArrayList<>();
    private static final List<Transaction> TRANSACTIONS = new ArrayList<>();
    private static final List<Loan> LOANS = new ArrayList<>();
    private static final List<LoanPayment> LOAN_PAYMENTS = new ArrayList<>();

    // ======= API thêm dữ liệu =======
    public static void addAccount(Account a)            { ACCOUNTS.add(a); }
    public static void addTransaction(Transaction t)    { TRANSACTIONS.add(t); }
    public static void addLoan(Loan l)                  { LOANS.add(l); }
    public static void addLoanPayment(LoanPayment p)    { LOAN_PAYMENTS.add(p); }

    // ======= Getters cho Exporters =======
    public static List<Account>      accounts()      { return ACCOUNTS; }
    public static List<Transaction>  transactions()  { return TRANSACTIONS; }
    public static List<Loan>         loans()         { return LOANS; }
    public static List<LoanPayment>  loanPayments()  { return LOAN_PAYMENTS; }

    // ======= Tiện ích dọn bộ đệm sau khi Lưu (tuỳ chọn) =======
    public static void clearAll() {
        ACCOUNTS.clear();
        TRANSACTIONS.clear();
        LOANS.clear();
        LOAN_PAYMENTS.clear();
    }
}
