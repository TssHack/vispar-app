@echo off
REM Script to verify Gradle wrapper integrity on Windows

echo Verifying Gradle wrapper integrity...

REM Check if gradle-wrapper.jar exists
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo ERROR: gradle-wrapper.jar not found!
    exit /b 1
)

REM Check if checksum file exists
if not exist "gradle\wrapper\gradle-wrapper.jar.sha256" (
    echo ERROR: gradle-wrapper.jar.sha256 not found!
    exit /b 1
)

REM Verify checksum using certutil (Windows built-in)
cd gradle\wrapper
for /f "tokens=*" %%a in ('certutil -hashfile gradle-wrapper.jar SHA256 ^| findstr /v "hash"') do set COMPUTED_CHECKSUM=%%a
set /p STORED_CHECKSUM=<gradle-wrapper.jar.sha256

REM Remove any trailing spaces from the stored checksum
for /f "tokens=*" %%b in ("%STORED_CHECKSUM%") do set STORED_CHECKSUM=%%b

if "%COMPUTED_CHECKSUM%"=="%STORED_CHECKSUM%" (
    echo ✓ Gradle wrapper checksum verification PASSED
    echo Checksum: %STORED_CHECKSUM%
    exit /b 0
) else (
    echo ✗ Gradle wrapper checksum verification FAILED
    echo Computed: %COMPUTED_CHECKSUM%
    echo Stored:   %STORED_CHECKSUM%
    exit /b 1
)