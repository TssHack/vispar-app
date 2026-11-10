@echo off
REM Script to fix Gradle wrapper validation issues on Windows

REM Function to retry a command
:retry_command
setlocal
set cmd=%1
set max_retries=3
set retry=0

:retry_loop
if %retry% GEQ %max_retries% (
    echo Failed to execute command after %max_retries% attempts
    exit /b 1
)

echo Attempt %retry% to execute: %cmd%
call %cmd%
if %ERRORLEVEL% EQU 0 (
    echo Command executed successfully
    exit /b 0
) else (
    echo Command failed, retrying in 5 seconds...
    set /a retry+=1
    timeout /t 5 /nobreak >nul
    goto retry_loop
)
endlocal

echo Fixing Gradle wrapper validation issues...

REM Update Gradle wrapper with retry
set retry=0
set max_retries=3

:update_loop
if %retry% GEQ %max_retries% (
    echo Failed to update Gradle wrapper after %max_retries% attempts
    exit /b 1
)

echo Attempt %retry% to update Gradle wrapper
call gradlew wrapper --gradle-version 8.11.1
if %ERRORLEVEL% EQU 0 (
    echo Gradle wrapper updated successfully
    goto generate_checksum
) else (
    echo Failed to update Gradle wrapper, retrying in 5 seconds...
    set /a retry+=1
    timeout /t 5 /nobreak >nul
    goto update_loop
)

:generate_checksum
REM Generate new checksums
cd gradle\wrapper

REM Remove old checksum file
del /f gradle-wrapper.jar.sha256 >nul 2>&1

REM Generate new checksum using certutil (Windows built-in)
certutil -hashfile gradle-wrapper.jar SHA256 | findstr /v "hash" > temp.txt
set /p hash=<temp.txt
echo %hash%> gradle-wrapper.jar.sha256
del temp.txt

echo Gradle wrapper fixed successfully!
type gradle-wrapper.jar.sha256