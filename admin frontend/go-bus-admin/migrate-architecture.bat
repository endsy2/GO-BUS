@echo off
REM Architecture Migration Script for Windows
REM Migrates from Atomic Design to Component-Based + Feature-Based Architecture

echo.
echo ========================================
echo   Architecture Migration Script
echo ========================================
echo.

echo [1/7] Creating directory structure...
mkdir src\shared\components\ui 2>nul
mkdir src\shared\components\common 2>nul
mkdir src\shared\components\layout 2>nul
mkdir src\shared\components\feedback 2>nul
mkdir src\shared\hooks 2>nul
mkdir src\shared\context 2>nul
mkdir src\shared\utils 2>nul
mkdir src\shared\constants 2>nul

mkdir src\features\auth\pages 2>nul
mkdir src\features\auth\services 2>nul

mkdir src\features\dashboard\components 2>nul
mkdir src\features\dashboard\hooks 2>nul
mkdir src\features\dashboard\pages 2>nul

mkdir src\features\buses\components 2>nul
mkdir src\features\buses\hooks 2>nul
mkdir src\features\buses\services 2>nul
mkdir src\features\buses\pages 2>nul

mkdir src\features\routes\components 2>nul
mkdir src\features\routes\hooks 2>nul
mkdir src\features\routes\services 2>nul
mkdir src\features\routes\pages 2>nul

mkdir src\features\customers\components 2>nul
mkdir src\features\customers\hooks 2>nul
mkdir src\features\customers\services 2>nul
mkdir src\features\customers\pages 2>nul

mkdir src\features\bookings\components 2>nul
mkdir src\features\bookings\hooks 2>nul
mkdir src\features\bookings\services 2>nul
mkdir src\features\bookings\pages 2>nul

mkdir src\features\reports\components 2>nul
mkdir src\features\reports\services 2>nul
mkdir src\features\reports\pages 2>nul

mkdir src\features\team\components 2>nul
mkdir src\features\team\hooks 2>nul
mkdir src\features\team\services 2>nul
mkdir src\features\team\pages 2>nul

echo [2/7] Copying UI components...
xcopy /Y src\components\ui\*.jsx src\shared\components\ui\ 2>nul

echo [3/7] Copying common components...
copy /Y src\components\atoms\Button\Button.js src\shared\components\common\Button.jsx 2>nul
copy /Y src\components\atoms\Badge\Badge.js src\shared\components\common\Badge.jsx 2>nul
copy /Y src\components\atoms\Icon\Icon.js src\shared\components\common\Icon.jsx 2>nul
copy /Y src\components\atoms\Input\Input.js src\shared\components\common\Input.jsx 2>nul
copy /Y src\components\atoms\Snackbar\Snackbar.js src\shared\components\common\Snackbar.jsx 2>nul

echo [4/7] Copying layout components...
copy /Y src\components\organisms\Sidebar\Sidebar.js src\shared\components\layout\Sidebar.jsx 2>nul
copy /Y src\components\molecules\TopBar\TopBar.js src\shared\components\layout\TopBar.jsx 2>nul
copy /Y src\components\molecules\NavItem\NavItem.js src\shared\components\layout\NavItem.jsx 2>nul

echo [5/7] Copying feedback components...
copy /Y src\components\molecules\ConfirmDialog\ConfirmDialog.js src\shared\components\feedback\ConfirmDialog.jsx 2>nul
copy /Y src\components\molecules\UnauthorizedDialog\UnauthorizedDialog.js src\shared\components\feedback\UnauthorizedDialog.jsx 2>nul
copy /Y src\components\molecules\Pagination\Pagination.js src\shared\components\feedback\Pagination.jsx 2>nul

echo [6/7] Copying context and utils...
copy /Y src\context\ThemeContext.js src\shared\context\ThemeContext.jsx 2>nul
copy /Y src\context\LocaleContext.js src\shared\context\LocaleContext.jsx 2>nul
xcopy /Y src\utils\*.js src\shared\utils\ 2>nul

echo [7/7] Copying feature files...

REM Auth
copy /Y src\components\pages\LoginPage\LoginPage.js src\features\auth\pages\LoginPage.jsx 2>nul
copy /Y src\services\authService.js src\features\auth\services\authService.js 2>nul

REM Dashboard
copy /Y src\components\pages\DashboardPage\DashboardPage.js src\features\dashboard\pages\DashboardPage.jsx 2>nul
copy /Y src\components\organisms\StatsGrid\StatsGrid.js src\features\dashboard\components\StatsGrid.jsx 2>nul
copy /Y src\components\molecules\StatCard\StatCard.js src\features\dashboard\components\StatCard.jsx 2>nul
copy /Y src\components\organisms\BookingTable\BookingTable.js src\features\dashboard\components\BookingTable.jsx 2>nul
copy /Y src\hooks\useStats.js src\features\dashboard\hooks\useStats.js 2>nul
copy /Y src\hooks\useBookings.js src\features\dashboard\hooks\useBookings.js 2>nul

REM Buses
copy /Y src\components\pages\BusesPage\BusesPage.js src\features\buses\pages\BusesPage.jsx 2>nul
copy /Y src\components\pages\BusDetailPage\BusDetailPage.js src\features\buses\pages\BusDetailPage.jsx 2>nul
copy /Y src\components\pages\CreateBusPage\CreateBusPage.js src\features\buses\pages\CreateBusPage.jsx 2>nul
copy /Y src\components\molecules\EditBusDialog\EditBusDialog.js src\features\buses\components\EditBusDialog.jsx 2>nul
copy /Y src\services\busService.js src\features\buses\services\busService.js 2>nul

REM Routes
copy /Y src\components\pages\RoutesPage\RoutesPage.js src\features\routes\pages\RoutesPage.jsx 2>nul
copy /Y src\components\pages\RouteDetailPage\RouteDetailPage.js src\features\routes\pages\RouteDetailPage.jsx 2>nul
copy /Y src\components\pages\createRoutePage\createRoutePage.js src\features\routes\pages\CreateRoutePage.jsx 2>nul
copy /Y src\services\routeService.js src\features\routes\services\routeService.js 2>nul

REM Customers
copy /Y src\components\pages\CustomersPage\CustomersPage.jsx src\features\customers\pages\CustomersPage.jsx 2>nul
copy /Y src\components\pages\CustomerDetailPage\CustomerDetailPage.jsx src\features\customers\pages\CustomerDetailPage.jsx 2>nul
copy /Y src\components\pages\CreateCustomerPage\CreateCustomerPage.js src\features\customers\pages\CreateCustomerPage.jsx 2>nul
copy /Y src\components\molecules\CreateCustomerDialog\CreateCustomerDialog.js src\features\customers\components\CreateCustomerDialog.jsx 2>nul
copy /Y src\components\molecules\EditCustomerDialog\EditCustomerDialog.jsx src\features\customers\components\EditCustomerDialog.jsx 2>nul
copy /Y src\services\customerService.js src\features\customers\services\customerService.js 2>nul

REM Bookings
copy /Y src\components\pages\BookingsPage\BookingsPage.js src\features\bookings\pages\BookingsPage.jsx 2>nul
copy /Y src\services\bookingService.js src\features\bookings\services\bookingService.js 2>nul

REM Reports
copy /Y src\components\pages\ReportsPage\ReportsPage.js src\features\reports\pages\ReportsPage.jsx 2>nul
copy /Y src\services\reportService.js src\features\reports\services\reportService.js 2>nul

REM Team
copy /Y src\components\pages\TeamPage\TeamPage.js src\features\team\pages\TeamPage.jsx 2>nul
copy /Y src\components\molecules\AssignRoleDialog\AssignRoleDialog.js src\features\team\components\AssignRoleDialog.jsx 2>nul
copy /Y src\services\userService.js src\features\team\services\userService.js 2>nul

echo.
echo ========================================
echo   Migration Complete!
echo ========================================
echo.
echo IMPORTANT NEXT STEPS:
echo 1. Update import paths in all copied files
echo 2. Verify jsconfig.json exists
echo 3. Update App.js routing
echo 4. Test the application
echo 5. Remove old directories after verification
echo.
echo See QUICK_START_MIGRATION.md for detailed instructions
echo.
pause
