import os, shutil, re, time, json

# --- CHỈNH LẠI CHO ĐÚNG ---
PROJECT_ROOT = r"D:\OOP\OOP_CK\src\main\java\app"  # thư mục CHỨA app\account, app\menu, ...
OUTPUT_ROOT   = os.path.join(os.path.dirname(PROJECT_ROOT), "app_refactored")

# ÁNH XẠ (như bản trước)
mapping = {
  "account/FinanceManager.java": "service/FinanceManager.java",
  "account/Account.java": "model/Account.java",
  "account/BankAccount.java": "model/BankAccount.java",
  "account/EWalletAccount.java": "model/EWalletAccount.java",
  "account/Transaction.java": "model/Transaction.java",
  "account/Ledger.java": "model/Ledger.java",
  "loan/Contract.java": "model/Contract.java",
  "payment/DateRange.java": "model/DateRange.java",

  "account/AccountActions.java": "service/AccountService.java",
  "transaction/TransactionService.java": "service/TransactionService.java",
  "account/TransferService.java": "service/TransferService.java",
  "account/ReportService.java": "service/ReportService.java",
  "loan/InterestService.java": "service/LoanInterestService.java",
  "loan/DateService.java": "service/DateService.java",

  "store/DataStore.java": "repository/DataStore.java",
  "account/AccountCsvStorage.java": "repository/AccountCsvStorage.java",
  "loan/ContractStorage.java": "repository/ContractStorage.java",

  "payment/AccountReport.java": "report/AccountReport.java",
  "payment/IncomeExpenseReport.java": "report/IncomeExpenseReport.java",
  "payment/LoanReport.java": "report/LoanReport.java",
  "payment/PaymentReportSaver.java": "report/PaymentReportSaver.java",
  "payment/ReportUtils.java": "report/ReportUtils.java",

  "export/CsvExporter.java": "export/CsvExporter.java",
  "export/ExportAccounts.java": "export/ExportAccounts.java",
  "export/ExportLoans.java": "export/ExportLoans.java",
  "export/ExportLoanPayments.java": "export/ExportLoanPayments.java",
  "export/ExportTransactions.java": "export/ExportTransactions.java",

  "menu/AccountMenu.java": "ui/menu/AccountMenu.java",
  "menu/TransferMenu.java": "ui/menu/TransferMenu.java",
  "menu/ReportMenu.java": "ui/menu/ReportMenu.java",
  "menu/ExportMenu.java": "ui/menu/ExportMenu.java",
  "menu/MenuIncomeExpense.java": "ui/menu/IncomeExpenseMenu.java",
  "menu/Menutransaction.java": "ui/menu/TransactionMenu.java",
  "menu/MenuPayment.java": "ui/menu/PaymentMenu.java",
  "menu/MenuLoan.java": "ui/menu/LoanMenu.java",
  "menu/MenuContact.java": "ui/menu/LoanContactMenu.java",
  "menu/MenuAccount.java": "ui/menu/MainMenu.java",

  "loan/MakeContact.java": "loan/LoanCreateMenu.java",
  "loan/UpdateContact.java": "loan/LoanUpdateMenu.java",
  "loan/DeleteContact.java": "loan/LoanDeleteMenu.java",
  "loan/readInfor.java": "loan/LoanReadInfo.java",
  "loan/ValidInfor.java": "loan/LoanValidator.java",
  "loan/CheckInfor.java": "loan/LoanInputCheck.java",
  "loan/ConvertDetail.java": "loan/LoanConvertDetail.java",

  "util/ConsoleUtils.java": "util/ConsoleUtils.java",
  "util/DateUtils.java": "util/DateUtils.java",
  "util/ConsoleMoneyReader.java": "util/ConsoleMoneyReader.java",

  "App.java": "main/App.java",
  "TestRunner.java": "dev/TestRunner.java",
  "TestScript.java": "dev/TestScript.java",
}

def ensure_dir(p):
    os.makedirs(p, exist_ok=True)

def rewrite_package(file_path, new_pkg):
    with open(file_path, "r", encoding="utf-8", errors="ignore") as fh:
        s = fh.read()
    pkg_re = re.compile(r"^\s*package\s+[^;]+;\s*$", re.MULTILINE)
    new_line = f"package {new_pkg};"
    if pkg_re.search(s):
        s = pkg_re.sub(new_line, s, count=1)
    else:
        s = new_line + "\n\n" + s
    with open(file_path, "w", encoding="utf-8") as fh:
        fh.write(s)

def safe_copy(src, dst, retries=2, delay=0.4):
    for i in range(retries+1):
        try:
            shutil.copy2(src, dst)
            return True
        except PermissionError as e:
            if i < retries:
                time.sleep(delay)
            else:
                print(f"[skip locked] {src} -> {dst} ({e})")
                return False

def main():
    print(f"PROJECT_ROOT = {PROJECT_ROOT}")
    print(f"OUTPUT_ROOT  = {OUTPUT_ROOT}")
    ensure_dir(OUTPUT_ROOT)

    for src_rel, dst_rel in mapping.items():
        src = os.path.join(PROJECT_ROOT, src_rel)
        if not os.path.exists(src):
            print("[skip missing]", src_rel)
            continue

        dst = os.path.join(OUTPUT_ROOT, dst_rel)
        dst_dir = os.path.dirname(dst)
        ensure_dir(dst_dir)

        ok = safe_copy(src, dst)
        if not ok:
            continue

        # tính package theo đường dẫn trong OUTPUT_ROOT
        pkg_parts = dst_rel.replace("\\", "/").split("/")[:-1]
        new_pkg = "app." + ".".join(pkg_parts) if pkg_parts else "app"
        try:
            rewrite_package(dst, new_pkg)
            print("[moved]", src_rel, "->", dst_rel, "package:", new_pkg)
        except Exception as e:
            print("[warn] rewrite package failed:", dst_rel, e)

    print("\nDONE. Code đã được xuất sang:", OUTPUT_ROOT)
    print("Hãy mở project theo src/main/java/app_refactored để build & chỉnh import nếu cần.")

if __name__ == "__main__":
    main()
