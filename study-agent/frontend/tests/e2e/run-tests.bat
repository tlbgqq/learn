@echo off
REM E2E Test Runner for Sprint Mode (Windows)
REM Usage: run-tests.bat [headed] [debug]

cd /d "%~dp0\..\.."

set HEADLESS=--project=chromium

:parse_args
if "%~1"=="" goto :run
if "%~1"=="headed" (
    set HEADLESS=
    shift
    goto :parse_args
)
if "%~1"=="debug" (
    set DEBUG=--debug
    shift
    goto :parse_args
)
shift
goto :parse_args

:run
echo ==========================================
echo E2E Test Runner - Sprint Mode
echo ==========================================

call npx playwright test %DEBUG% %HEADLESS%

echo.
echo ==========================================
echo Tests complete!
echo ==========================================
pause
