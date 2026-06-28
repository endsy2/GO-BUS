#!/bin/bash

# Architecture Migration Script
# Migrates from Atomic Design to Component-Based + Feature-Based Architecture

echo "🚀 Starting Architecture Migration..."

# Create directory structure
echo "📁 Creating new directory structure..."
mkdir -p src/shared/components/{ui,common,layout,feedback}
mkdir -p src/shared/{hooks,context,utils,constants}
mkdir -p src/features/{auth,dashboard,buses,routes,customers,bookings,reports,team}/{components,hooks,services,pages}

# Phase 1: Copy UI Components
echo "📦 Copying UI components..."
cp src/components/ui/*.jsx src/shared/components/ui/ 2>/dev/null || true

# Phase 2: Copy Common Components
echo "📦 Copying common components..."
cp src/components/atoms/Button/Button.js src/shared/components/common/Button.jsx
cp src/components/atoms/Badge/Badge.js src/shared/components/common/Badge.jsx
cp src/components/atoms/Icon/Icon.js src/shared/components/common/Icon.jsx
cp src/components/atoms/Input/Input.js src/shared/components/common/Input.jsx
cp src/components/atoms/Snackbar/Snackbar.js src/shared/components/common/Snackbar.jsx

# Phase 3: Copy Layout Components
echo "📦 Copying layout components..."
cp src/components/organisms/Sidebar/Sidebar.js src/shared/components/layout/Sidebar.jsx
cp src/components/molecules/TopBar/TopBar.js src/shared/components/layout/TopBar.jsx
cp src/components/molecules/NavItem/NavItem.js src/shared/components/layout/NavItem.jsx

# Phase 4: Copy Feedback Components
echo "📦 Copying feedback components..."
cp src/components/molecules/ConfirmDialog/ConfirmDialog.js src/shared/components/feedback/ConfirmDialog.jsx
cp src/components/molecules/UnauthorizedDialog/UnauthorizedDialog.js src/shared/components/feedback/UnauthorizedDialog.jsx
cp src/components/molecules/Pagination/Pagination.js src/shared/components/feedback/Pagination.jsx

# Phase 5: Copy Context
echo "📦 Copying context..."
cp src/context/ThemeContext.js src/shared/context/ThemeContext.jsx
cp src/context/LocaleContext.js src/shared/context/LocaleContext.jsx

# Phase 6: Copy Utils
echo "📦 Copying utils..."
cp src/utils/*.js src/shared/utils/

# Phase 7: Copy Feature Files
echo "📦 Copying feature files..."

# Auth
cp src/components/pages/LoginPage/LoginPage.js src/features/auth/pages/LoginPage.jsx
cp src/services/authService.js src/features/auth/services/authService.js

# Dashboard
cp src/components/pages/DashboardPage/DashboardPage.js src/features/dashboard/pages/DashboardPage.jsx
cp src/components/organisms/StatsGrid/StatsGrid.js src/features/dashboard/components/StatsGrid.jsx
cp src/components/molecules/StatCard/StatCard.js src/features/dashboard/components/StatCard.jsx
cp src/components/organisms/BookingTable/BookingTable.js src/features/dashboard/components/BookingTable.jsx
cp src/hooks/useStats.js src/features/dashboard/hooks/useStats.js
cp src/hooks/useBookings.js src/features/dashboard/hooks/useBookings.js

# Buses
cp src/components/pages/BusesPage/BusesPage.js src/features/buses/pages/BusesPage.jsx
cp src/components/pages/BusDetailPage/BusDetailPage.js src/features/buses/pages/BusDetailPage.jsx
cp src/components/pages/CreateBusPage/CreateBusPage.js src/features/buses/pages/CreateBusPage.jsx
cp src/components/molecules/EditBusDialog/EditBusDialog.js src/features/buses/components/EditBusDialog.jsx
cp src/services/busService.js src/features/buses/services/busService.js

# Routes
cp src/components/pages/RoutesPage/RoutesPage.js src/features/routes/pages/RoutesPage.jsx
cp src/components/pages/RouteDetailPage/RouteDetailPage.js src/features/routes/pages/RouteDetailPage.jsx
cp src/components/pages/createRoutePage/createRoutePage.js src/features/routes/pages/CreateRoutePage.jsx
cp src/services/routeService.js src/features/routes/services/routeService.js

# Customers
cp src/components/pages/CustomersPage/CustomersPage.jsx src/features/customers/pages/CustomersPage.jsx
cp src/components/pages/CustomerDetailPage/CustomerDetailPage.jsx src/features/customers/pages/CustomerDetailPage.jsx
cp src/components/pages/CreateCustomerPage/CreateCustomerPage.js src/features/customers/pages/CreateCustomerPage.jsx
cp src/components/molecules/CreateCustomerDialog/CreateCustomerDialog.js src/features/customers/components/CreateCustomerDialog.jsx
cp src/components/molecules/EditCustomerDialog/EditCustomerDialog.jsx src/features/customers/components/EditCustomerDialog.jsx
cp src/components/molecules/CustomerRow/CustomerRow.js src/features/customers/components/CustomerRow.jsx 2>/dev/null || true
cp src/services/customerService.js src/features/customers/services/customerService.js

# Bookings
cp src/components/pages/BookingsPage/BookingsPage.js src/features/bookings/pages/BookingsPage.jsx
cp src/services/bookingService.js src/features/bookings/services/bookingService.js

# Reports
cp src/components/pages/ReportsPage/ReportsPage.js src/features/reports/pages/ReportsPage.jsx
cp src/services/reportService.js src/features/reports/services/reportService.js

# Team
cp src/components/pages/TeamPage/TeamPage.js src/features/team/pages/TeamPage.jsx
cp src/components/molecules/AssignRoleDialog/AssignRoleDialog.js src/features/team/components/AssignRoleDialog.jsx
cp src/services/userService.js src/features/team/services/userService.js

echo "✅ Files copied successfully!"
echo ""
echo "⚠️  IMPORTANT NEXT STEPS:"
echo "1. Update import paths in all copied files"
echo "2. Create jsconfig.json with path aliases"
echo "3. Create index.js files for each feature"
echo "4. Update App.js routing"
echo "5. Test the application"
echo "6. Remove old directories after verification"
echo ""
echo "📖 See MIGRATION_SCRIPT.md for detailed instructions"
