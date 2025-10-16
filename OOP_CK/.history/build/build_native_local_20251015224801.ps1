# PowerShell script with steps to install GraalVM and native-image on Windows, then build native binary
# Run as Administrator

# 1. Download GraalVM (example: GraalVM CE Java 21 or 25) and extract
# Example (manual): https://github.com/graalvm/graalvm-ce-builds/releases

# 2. Set GRAALVM_HOME to extracted folder and add to PATH:
# $env:GRAALVM_HOME = 'C:\path\to\graalvm'
# [Environment]::SetEnvironmentVariable('GRAALVM_HOME', $env:GRAALVM_HOME, 'User')
# $oldPath = [Environment]::GetEnvironmentVariable('Path', 'User')
# [Environment]::SetEnvironmentVariable('Path', $oldPath + ';' + $env:GRAALVM_HOME + '\bin', 'User')

# 3. Open new PowerShell to pick up PATH, then install native-image:
# gu install native-image

# 4. Build native image (from project root):
# cd D:\OOP\OOP_CK\build
# native-image --no-fallback -jar app.jar -H:Name=oop_ck_win.exe

# 5. Resulting native exe will be in build\oop_ck_win.exe

Write-Output "Script provides steps only. Follow instructions above to build native image on Windows."