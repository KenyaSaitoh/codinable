@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ============================================
echo  Codinable - Setup
echo ============================================
echo.
echo Java / Node.js / Python / Bash / jdtls / HSQLDB を用意します。
echo 初回は 10 分以上かかる場合があります。
echo.

powershell -ExecutionPolicy Bypass -File "%~dp0scripts\setup.ps1" %*
if errorlevel 1 (
    echo.
    echo [FAIL] セットアップに失敗しました。上のメッセージを確認してください。
    pause
    exit /b 1
)

echo.
echo ============================================
echo  npm install
echo ============================================
pushd "%~dp0desktop"
call npm install
if errorlevel 1 (
    popd
    echo.
    echo [FAIL] npm install に失敗しました。
    pause
    exit /b 1
)
popd

echo.
echo [OK] 準備ができました。起動するには:
echo        cd desktop
echo        npm start
echo.
pause
