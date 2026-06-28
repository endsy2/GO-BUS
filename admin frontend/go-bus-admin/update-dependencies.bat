@echo off
echo Updating package-lock.json...
echo.

REM Delete node_modules and package-lock.json
if exist node_modules (
    echo Removing old node_modules...
    rmdir /s /q node_modules
)

if exist package-lock.json (
    echo Removing old package-lock.json...
    del package-lock.json
)

REM Install dependencies with legacy peer deps
echo Installing dependencies...
npm install --legacy-peer-deps

echo.
echo Done! Dependencies updated.
echo You can now build the Docker image.
pause
