
    import os, shutil, re, pathlib

    PROJECT_ROOT = r"/mnt/data/workspace_app/app"
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
  "TestScript.java": "dev/TestScript.java"
}

    def ensure_dir(p):
        os.makedirs(p, exist_ok=True)

    def rewrite_package(file_path, new_pkg):
        # Read file
        with open(file_path, "r", encoding="utf-8", errors="ignore") as fh:
            s = fh.read()
        # Replace or insert package declaration
        pkg_line_re = re.compile(r"^\s*package\s+[^;]+;\s*$", re.MULTILINE)
        new_line = f"package {new_pkg};"
        if pkg_line_re.search(s):
            s = pkg_line_re.sub(new_line, s, count=1)
        else:
            s = new_line + "\n\n" + s
        with open(file_path, "w", encoding="utf-8") as fh:
            fh.write(s)

    def main():
        for src_rel, dst_rel in mapping.items():
            src = os.path.join(PROJECT_ROOT, src_rel)
            dst = os.path.join(PROJECT_ROOT, dst_rel)
            if not os.path.exists(src):
                print("[skip missing]", src_rel)
                continue
            dst_dir = os.path.dirname(dst)
            ensure_dir(dst_dir)
            shutil.copy2(src, dst)
            # Determine new package by path under /app/
            pkg_parts = dst_rel.split(os.sep)[:-1]
            # map first segment to package base 'app'
            if pkg_parts:
                new_pkg = "app." + ".".join(pkg_parts)
            else:
                new_pkg = "app"
            rewrite_package(dst, new_pkg)
            print("[moved]", src_rel, "->", dst_rel, "package:", new_pkg)
        print("\nDONE. Verify and then remove old files manually if satisfied.")

    if __name__ == "__main__":
        main()
