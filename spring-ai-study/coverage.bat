@echo off
REM Coverage Report Generation Script for Windows
REM This script runs tests and generates JaCoCo coverage reports

echo ========================================
echo Spring AI Study - Coverage Report Tool
echo ========================================

if "%1"=="help" (
    echo Usage:
    echo   coverage.bat [command] [profile]
    echo.
    echo Commands:
    echo   unit      - Run unit tests only with coverage
    echo   integration - Run integration tests only with coverage
    echo   all       - Run all tests with coverage ^(default^)
    echo   report    - Generate coverage report without running tests
    echo   clean     - Clean previous coverage data
    echo   open      - Open coverage report in browser
    echo   help      - Show this help message
    echo.
    echo Profiles:
    echo   dev       - Development profile ^(70%% coverage threshold^)
    echo   ci        - CI/CD profile ^(85%% coverage threshold^)
    echo   ^(default^) - Standard profile ^(80%% coverage threshold^)
    echo.
    echo Examples:
    echo   coverage.bat all dev
    echo   coverage.bat unit
    echo   coverage.bat open
    goto :eof
)

set COMMAND=%1
set PROFILE=%2

if "%COMMAND%"=="" set COMMAND=all
if "%PROFILE%"=="" (
    set MAVEN_PROFILE=
) else (
    set MAVEN_PROFILE=-P%PROFILE%
)

echo Command: %COMMAND%
if not "%PROFILE%"=="" echo Profile: %PROFILE%
echo.

if "%COMMAND%"=="clean" (
    echo Cleaning previous coverage data...
    call mvn clean %MAVEN_PROFILE%
    goto :eof
)

if "%COMMAND%"=="unit" (
    echo Running unit tests with coverage...
    call mvn clean test jacoco:report %MAVEN_PROFILE%
    goto :show_results
)

if "%COMMAND%"=="integration" (
    echo Running integration tests with coverage...
    call mvn clean verify -DskipUnitTests=true %MAVEN_PROFILE%
    goto :show_results
)

if "%COMMAND%"=="all" (
    echo Running all tests with coverage...
    call mvn clean verify %MAVEN_PROFILE%
    goto :show_results
)

if "%COMMAND%"=="report" (
    echo Generating coverage report...
    call mvn jacoco:report %MAVEN_PROFILE%
    goto :show_results
)

if "%COMMAND%"=="open" (
    echo Opening coverage report...
    if exist "target\site\jacoco\index.html" (
        start target\site\jacoco\index.html
    ) else if exist "target\site\jacoco-merged\index.html" (
        start target\site\jacoco-merged\index.html
    ) else (
        echo Coverage report not found. Please run tests first.
        echo Use: coverage.bat all
    )
    goto :eof
)

echo Unknown command: %COMMAND%
echo Use 'coverage.bat help' for usage information.
goto :eof

:show_results
echo.
echo ========================================
echo Coverage Report Generation Complete
echo ========================================
echo.
echo Reports generated in:
if exist "target\site\jacoco\index.html" (
    echo   Unit Tests: target\site\jacoco\index.html
)
if exist "target\site\jacoco-it\index.html" (
    echo   Integration Tests: target\site\jacoco-it\index.html
)
if exist "target\site\jacoco-merged\index.html" (
    echo   Merged Report: target\site\jacoco-merged\index.html
)
echo.
echo To open the report in your browser:
echo   coverage.bat open
echo.