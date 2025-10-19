package app.repository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Bộ nhớ tạm (in-memory) lưu dữ liệu dạng List&lt;Map&lt;String,Object&gt;&gt; cho tính năng xuất CSV.
 * Mỗi lần ghi, dữ liệu được sao chép sang Map bất biến để tránh chỉnh sửa ngoài ý muốn.
 */
public final class DataStore {
    private DataStore() {}

    /** Các key dành cho bản ghi tài khoản. */
    public static final class AccountFields {
        private AccountFields() {}
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String TYPE = "type";
        public static final String BALANCE = "balance";
        public static final String NOTE = "note";
    }

    /** Các key dành cho bản ghi giao dịch. */
    public static final class TransactionFields {
        private TransactionFields() {}
        public static final String ID = "id";
        public static final String DATE = "date";
        public static final String TYPE = "type";
        public static final String AMOUNT = "amount";
        public static final String ACCOUNT_ID = "accountId";
        public static final String ACCOUNT_NAME = "accountName";
        public static final String CATEGORY = "category";
        public static final String NOTE = "note";
    }

    /** Các key dành cho bản ghi khoản vay. */
    public static final class LoanFields {
        private LoanFields() {}
        public static final String ID = "loanId";
        public static final String STATUS = "status";
        public static final String NAME = "name";
        public static final String AMOUNT = "amount";
        public static final String PHONE = "phone";
        public static final String BORROW_DATE = "borrowDate";
        public static final String DUE_DATE = "dueDate";
        public static final String INTEREST = "interest";
        public static final String TYPE = "interestType";
        public static final String NOTE = "note";
        public static final String CREATED_AT = "createdAt";
    }

    /** Các key dành cho bản ghi lịch sử trả nợ. */
    public static final class LoanPaymentFields {
        private LoanPaymentFields() {}
        public static final String LOAN_ID = "loanId";
        public static final String DATE = "date";
        public static final String AMOUNT = "amount";
        public static final String NOTE = "note";
    }

    // ======= Bộ đệm chính =======
    private static final List<Map<String, Object>> ACCOUNTS = new ArrayList<>();
    private static final List<Map<String, Object>> TRANSACTIONS = new ArrayList<>();

    // Các phần dữ liệu khác (loan) tạm thời vẫn lưu dưới dạng Map generic cho tới khi refactor riêng.
    private static final List<Map<String, Object>> LOANS = new ArrayList<>();
    private static final List<Map<String, Object>> LOAN_PAYMENTS = new ArrayList<>();

    // ======= API: Tài khoản =======
    public static void upsertAccount(Map<String, Object> row) {
        Map<String, Object> snapshot = snapshotAccount(row);
        String id = (String) snapshot.get(AccountFields.ID);
        replaceFirst(ACCOUNTS, r -> Objects.equals(r.get(AccountFields.ID), id), snapshot);
    }

    public static void removeAccountById(String id) {
        ACCOUNTS.removeIf(r -> Objects.equals(r.get(AccountFields.ID), id));
    }

    public static List<Map<String, Object>> accounts() {
        return Collections.unmodifiableList(ACCOUNTS);
    }

    // ======= API: Giao dịch =======
    public static void upsertTransaction(Map<String, Object> row) {
        Map<String, Object> snapshot = snapshotTransaction(row);
        String id = (String) snapshot.get(TransactionFields.ID);
        replaceFirst(TRANSACTIONS, r -> Objects.equals(r.get(TransactionFields.ID), id), snapshot);
    }

    public static void removeTransactionById(String id) {
        TRANSACTIONS.removeIf(r -> Objects.equals(r.get(TransactionFields.ID), id));
    }

    public static void updateTransactionsAccountName(String accountId, String newName) {
        TRANSACTIONS.replaceAll(row -> {
            if (Objects.equals(row.get(TransactionFields.ACCOUNT_ID), accountId)) {
                Map<String, Object> copy = new LinkedHashMap<>(row);
                copy.put(TransactionFields.ACCOUNT_NAME, newName);
                return Collections.unmodifiableMap(copy);
            }
            return row;
        });
    }

    public static List<Map<String, Object>> transactions() {
        return Collections.unmodifiableList(TRANSACTIONS);
    }

    // ======= API: Khoản vay (giữ dạng Map generic) =======
    public static void upsertLoan(Map<String, Object> row) {
        Map<String, Object> snapshot = snapshotLoan(row);
        String id = (String) snapshot.get(LoanFields.ID);
        replaceFirst(LOANS, r -> Objects.equals(r.get(LoanFields.ID), id), snapshot);
    }

    public static void removeLoanById(String id) {
        LOANS.removeIf(r -> Objects.equals(r.get(LoanFields.ID), id));
    }

    public static void replaceLoans(List<Map<String, Object>> rows) {
        LOANS.clear();
        for (Map<String, Object> row : rows) {
            LOANS.add(snapshotLoan(row));
        }
    }

    public static void addLoanPayment(Map<String, Object> row) {
        LOAN_PAYMENTS.add(snapshotLoanPayment(row));
    }
    public static List<Map<String, Object>> loans() { return Collections.unmodifiableList(LOANS); }
    public static List<Map<String, Object>> loanPayments() { return Collections.unmodifiableList(LOAN_PAYMENTS); }

    // ======= Dọn bộ đệm =======
    public static void clearAll() {
        ACCOUNTS.clear();
        TRANSACTIONS.clear();
        LOANS.clear();
        LOAN_PAYMENTS.clear();
    }

    // ======= Helpers =======
    private static Map<String, Object> snapshotAccount(Map<String, Object> row) {
        Objects.requireNonNull(row, "row");
        String id = (String) row.get(AccountFields.ID);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Account row must contain 'id'");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        copy.put(AccountFields.ID, id);
        copy.put(AccountFields.NAME, row.getOrDefault(AccountFields.NAME, ""));
        copy.put(AccountFields.TYPE, row.getOrDefault(AccountFields.TYPE, ""));
        copy.put(AccountFields.BALANCE, row.getOrDefault(AccountFields.BALANCE, "0"));
        copy.put(AccountFields.NOTE, row.getOrDefault(AccountFields.NOTE, ""));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, Object> snapshotTransaction(Map<String, Object> row) {
        Objects.requireNonNull(row, "row");
        String id = (String) row.get(TransactionFields.ID);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Transaction row must contain 'id'");
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        copy.put(TransactionFields.ID, id);
        copy.put(TransactionFields.DATE, row.getOrDefault(TransactionFields.DATE, ""));
        copy.put(TransactionFields.TYPE, row.getOrDefault(TransactionFields.TYPE, ""));
        copy.put(TransactionFields.AMOUNT, row.getOrDefault(TransactionFields.AMOUNT, "0"));
        copy.put(TransactionFields.ACCOUNT_ID, row.getOrDefault(TransactionFields.ACCOUNT_ID, ""));
        copy.put(TransactionFields.ACCOUNT_NAME, row.getOrDefault(TransactionFields.ACCOUNT_NAME, ""));
        copy.put(TransactionFields.CATEGORY, row.getOrDefault(TransactionFields.CATEGORY, ""));
        copy.put(TransactionFields.NOTE, row.getOrDefault(TransactionFields.NOTE, ""));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, Object> snapshotLoan(Map<String, Object> row) {
        Objects.requireNonNull(row, "row");
        Map<String, Object> copy = new LinkedHashMap<>();
        copy.put(LoanFields.ID, row.getOrDefault(LoanFields.ID, ""));
        copy.put(LoanFields.STATUS, row.getOrDefault(LoanFields.STATUS, ""));
        copy.put(LoanFields.NAME, row.getOrDefault(LoanFields.NAME, ""));
        copy.put(LoanFields.AMOUNT, row.getOrDefault(LoanFields.AMOUNT, ""));
        copy.put(LoanFields.PHONE, row.getOrDefault(LoanFields.PHONE, ""));
        copy.put(LoanFields.BORROW_DATE, row.getOrDefault(LoanFields.BORROW_DATE, ""));
        copy.put(LoanFields.DUE_DATE, row.getOrDefault(LoanFields.DUE_DATE, ""));
        copy.put(LoanFields.INTEREST, row.getOrDefault(LoanFields.INTEREST, ""));
        copy.put(LoanFields.TYPE, row.getOrDefault(LoanFields.TYPE, ""));
        copy.put(LoanFields.NOTE, row.getOrDefault(LoanFields.NOTE, ""));
        copy.put(LoanFields.CREATED_AT, row.getOrDefault(LoanFields.CREATED_AT, ""));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, Object> snapshotLoanPayment(Map<String, Object> row) {
        Objects.requireNonNull(row, "row");
        Map<String, Object> copy = new LinkedHashMap<>();
        copy.put(LoanPaymentFields.LOAN_ID, row.getOrDefault(LoanPaymentFields.LOAN_ID, ""));
        copy.put(LoanPaymentFields.DATE, row.getOrDefault(LoanPaymentFields.DATE, ""));
        copy.put(LoanPaymentFields.AMOUNT, row.getOrDefault(LoanPaymentFields.AMOUNT, ""));
        copy.put(LoanPaymentFields.NOTE, row.getOrDefault(LoanPaymentFields.NOTE, ""));
        return Collections.unmodifiableMap(copy);
    }

    /** Tạo bản sao bất biến (giữ nguyên nội dung hiện có). */
    private static Map<String, Object> unmodifiableCopy(Map<String, Object> row) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(row));
    }

    /** Thay thế phần tử đầu tiên khớp predicate; nếu chưa có thì thêm mới (giống semantics upsert). */
    private static void replaceFirst(List<Map<String, Object>> list,
                                     Predicate<Map<String, Object>> match,
                                     Map<String, Object> replacement) {
        for (int i = 0; i < list.size(); i++) {
            if (match.test(list.get(i))) {
                list.set(i, replacement);
                return;
            }
        }
        list.add(replacement);
    }
}
