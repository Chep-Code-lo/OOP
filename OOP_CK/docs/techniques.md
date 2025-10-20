# Kỹ Thuật Đã Sử Dụng Trong Dự Án

## Cấu Trúc Dữ Liệu

- **LinkedHashMap**
  - Dùng trong `FinanceManager` để lưu `Map<String, Account> accountIndex`.
  - Giữ nguyên thứ tự chèn giúp khi hiển thị danh sách tài khoản vẫn đúng thứ tự người dùng tạo.

- **List<Map<String, Object>>**
  - `DataStore` lưu tài khoản, giao dịch, hợp đồng vay bằng `List<Map<...>>`.
  - Mỗi map đại diện cho một bản ghi với cặp `key/value`, phù hợp với nhu cầu xuất CSV linh hoạt mà không cần class chuyên biệt.

- **Enum**
  - `TxnType`, `Contract.Stats`, `Contract.typeInterest` chuẩn hóa các giá trị (loại giao dịch, trạng thái lãi) và tránh lỗi chính tả.

- **Record**
  - `DateRange` triển khai bằng `record` để mang tính bất biến, mô tả khoảng ngày cho báo cáo.

## Kỹ Thuật & Pattern

- **Stream API**
  - `Ledger.query()` lọc giao dịch theo tài khoản, thời gian, loại.
  - `FinanceManager.addAccount()` dùng stream kiểm tra trùng tên.
  - `IncomeExpenseReport`, `ReportService` gom tổng thu/chi bằng `stream` + `Collectors`.

- **Builder Pattern**
  - `Transaction.builder()` giúp tạo giao dịch bằng chuỗi lời gọi `.accountId(...)`, tránh constructor dài và dễ mở rộng.

- **Facade / Service Layer**
  - `FinanceManager` đóng vai trò facade, điều phối `TransactionService`, `TransferService`, `ReportService`.
  - Giữ cho giao diện console (các menu) tách biệt khỏi nghiệp vụ.

- **Defensive Checks & Optional**
  - Mọi service kiểm tra dữ liệu đầu vào (`null`, giá trị âm) và ném exception rõ ràng.
  - `Ledger.find()` trả về `Optional<Transaction>` để tránh `null`.

- **Console Menu Pattern**
  - Mỗi menu (`AccountMenu`, `TransactionMenu`, `MenuLoan`, …) là một vòng lặp đọc lựa chọn, gọi dịch vụ rồi quay lại.
  - Helpers (`ConsoleUtils`, `ConsoleMoneyReader`, `ReadInfor`) chuẩn hóa nhập/xuất dữ liệu.

- **CSV Export Module**
  - `CsvExporter.writeCsv()` tự tạo thư mục `data`, ghi UTF-8 + BOM để Excel hiển thị tiếng Việt.
  - `ExportAccounts`, `ExportTransactions`, `ExportLoanPayments`, `PaymentReportSaver` định nghĩa format từng báo cáo.

## Quản Lý Lưu Trữ

- **ContractStorage**
  - Sinh ID hợp đồng theo tiền tố (Co/Ch + số thứ tự) và cờ `dirty` để flush CSV khi có thay đổi.
  - Đồng bộ song song với `DataStore` dạng `List<Map<...>>`.

- **AccountCsvStorage**
  - Ghi file `accounts.csv` mỗi khi tài khoản thay đổi, đảm bảo dữ liệu ngoại vi luôn đồng bộ.

## Tổng Kết

Dự án kết hợp nhiều kỹ thuật:
- Cấu trúc dữ liệu chuẩn (`LinkedHashMap`, `List<Map<...>>`, `Enum`, `record`).
- Pattern & API của Java (`Builder`, `Facade`, `Stream API`, defensive checks).
- Hệ thống menu console kèm helper tái sử dụng.
- Module xuất CSV và kiểm thử tự động hóa.

Nhờ đó, phần mềm console vẫn có kiến trúc rõ ràng, dễ bảo trì và tiện mở rộng theo nhu cầu quản lý tài chính cá nhân.
