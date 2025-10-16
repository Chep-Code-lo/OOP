@echo off
REM Extract the bundled ZIP (if present) and launch the portable EXE.
REM This batch runs in the extraction folder. We'll extract to a subfolder "extracted" and launch the launcher.
n
set ZIPNAME=OOP_CK-1.0-portable.zip
set EXTRACT_DIR=%~dp0extracted
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath \"%~dp0%ZIPNAME%\" -DestinationPath \"%EXTRACT_DIR%\" -Force"
nstart "" "%EXTRACT_DIR%\OOP_CK_Portable\OOP_CK_Portable.exe"
exit /b 0
