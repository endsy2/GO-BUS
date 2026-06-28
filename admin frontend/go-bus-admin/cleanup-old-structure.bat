@echo off
echo.
echo 🧹 Cleaning up old folder structure...
echo.
echo ⚠️  WARNING: This will delete the old components folder!
echo Make sure you have:
echo   1. Committed your current changes to git
echo   2. Verified the new structure works
echo   3. Tested the application
echo.
set /p confirm="Continue? (yes/no): "

if not "%confirm%"=="yes" (
    echo Cleanup cancelled.
    exit /b 0
)

echo.
echo Creating backup...
if not exist backups mkdir backups
xcopy /E /I /Y src\components backups\components_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2% >nul 2>&1
xcopy /E /I /Y src\hooks backups\hooks_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2% >nul 2>&1
xcopy /E /I /Y src\context backups\context_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2% >nul 2>&1
xcopy /E /I /Y src\utils backups\utils_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2% >nul 2>&1
xcopy /E /I /Y src\locales backups\locales_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2% >nul 2>&1

echo ✓ Backup created in backups\ folder
echo.
echo Removing old folders...

REM Remove old component structure
rmdir /S /Q src\components\atoms 2>nul
rmdir /S /Q src\components\molecules 2>nul
rmdir /S /Q src\components\organisms 2>nul
rmdir /S /Q src\components\pages 2>nul
rmdir /S /Q src\components\ui 2>nul
del /Q src\components\Dashboard.css 2>nul
del /Q src\components\Dashboard.js 2>nul
del /Q src\components\Sidebar.css 2>nul
del /Q src\components\Sidebar.js 2>nul

REM Remove the components folder if empty
rmdir src\components 2>nul

REM Remove old folders that have been moved to shared/
rmdir /S /Q src\hooks 2>nul
rmdir /S /Q src\context 2>nul
rmdir /S /Q src\utils 2>nul
rmdir /S /Q src\locales 2>nul

echo ✓ Old folders removed
echo.
echo ✅ Cleanup complete!
echo.
echo Your new structure:
echo   src\features\     - Feature modules
echo   src\shared\       - Shared resources
echo   src\services\     - Global services
echo   src\lib\          - Utilities
echo.
echo Next steps:
echo   1. Run: npm run build
echo   2. Fix any import errors
echo   3. Run: npm start
echo   4. Test all features
echo.
pause
