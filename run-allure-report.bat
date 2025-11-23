@echo off
REM ============================================
REM Run Allure report using local Allure bundle
REM ============================================

cd /d "%~dp0"

set RESULTS_DIR=allure-results
set ALLURE_BIN="%~dp0allure-2.35.1\bin\allure.bat"

if not exist "%RESULTS_DIR%" (
    echo [ERROR] Allure results not found at "%RESULTS_DIR%".
    echo Run "mvn test" first to generate results.
    pause
    exit /b 1
)

if not exist %ALLURE_BIN% (
    echo [ERROR] Allure binary not found at %ALLURE_BIN%.
    pause
    exit /b 1
)

echo Using Allure: %ALLURE_BIN%
%ALLURE_BIN% serve "%RESULTS_DIR%"
