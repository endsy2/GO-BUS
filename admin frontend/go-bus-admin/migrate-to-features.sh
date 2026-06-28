#!/bin/bash

echo "Starting Feature-Based Architecture Migration..."

# Create all feature directories
mkdir -p src/features/auth/{pages/LoginPage,services}
mkdir -p src/features/dashboard/{pages/DashboardPage,components/StatsGrid,hooks}
mkdir -p src/features/buses/{pages/{BusesPage,BusDetailPage,CreateBusPage},components/EditBusDialog,services}
mkdir -p src/features/routes/{pages/{RoutesPage,RouteDetailPage,CreateRoutePage},services}
mkdir -p src/features/customers/{pages/{CustomersPage,CustomerDetailPage,CreateCustomerPage},components/{EditCustomerDialog,CreateCustomerDialog},services}
mkdir -p src/features/bookings/{pages/BookingsPage,components/BookingTable,hooks,services}
mkdir -p src/features/reports/{pages/ReportsPage,services}
mkdir -p src/features/team/{pages/TeamPage,components/AssignRoleDialog,services}

echo "✓ Feature directories created"

# Copy Auth Feature
cp -r src/components/pages/LoginPage/* src/features/auth/pages/LoginPage/ 2>/dev/null
cp src/services/authService.js src/features/auth/services/ 2>/dev/null

# Copy Dashboard Feature  
cp -r src/components/pages/DashboardPage/* src/features/dashboard/pages/DashboardPage/ 2>/dev/null
cp -r src/components/organisms/StatsGrid/* src/features/dashboard/components/StatsGrid/ 2>/dev/null
cp src/shared/hooks/useStats.js src/features/dashboard/hooks/ 2>/dev/null

# Copy Buses Feature
cp -r src/components/pages/BusesPage/* src/features/buses/pages/BusesPage/ 2>/dev/null
cp -r src/components/pages/BusDetailPage/* src/features/buses/pages/BusDetailPage/ 2>/dev/null
cp -r src/components/pages/CreateBusPage/* src/features/buses/pages/CreateBusPage/ 2>/dev/null
cp -r src/components/molecules/EditBusDialog/* src/features/buses/components/EditBusDialog/ 2>/dev/null
cp src/services/busService.js src/features/buses/services/ 2>/dev/null

# Copy Routes Feature
cp -r src/components/pages/RoutesPage/* src/features/routes/pages/RoutesPage/ 2>/dev/null
cp -r src/components/pages/RouteDetailPage/* src/features/routes/pages/RouteDetailPage/ 2>/dev/null
cp -r src/components/pages/createRoutePage/* src/features/routes/pages/CreateRoutePage/ 2>/dev/null
cp src/services/routeService.js src/features/routes/services/ 2>/dev/null

# Copy Customers Feature
cp -r src/components/pages/CustomersPage/* src/features/customers/pages/CustomersPage/ 2>/dev/null
cp -r src/components/pages/CustomerDetailPage/* src/features/customers/pages/CustomerDetailPage/ 2>/dev/null
cp -r src/components/pages/CreateCustomerPage/* src/features/customers/pages/CreateCustomerPage/ 2>/dev/null
cp -r src/components/molecules/EditCustomerDialog/* src/features/customers/components/EditCustomerDialog/ 2>/dev/null
cp -r src/components/molecules/CreateCustomerDialog/* src/features/customers/components/CreateCustomerDialog/ 2>/dev/null
cp src/services/customerService.js src/features/customers/services/ 2>/dev/null

# Copy Bookings Feature
cp -r src/components/pages/BookingsPage/* src/features/bookings/pages/BookingsPage/ 2>/dev/null
cp -r src/components/organisms/BookingTable/* src/features/bookings/components/BookingTable/ 2>/dev/null
cp src/shared/hooks/useBookings.js src/features/bookings/hooks/ 2>/dev/null
cp src/services/bookingService.js src/features/bookings/services/ 2>/dev/null

# Copy Reports Feature
cp -r src/components/pages/ReportsPage/* src/features/reports/pages/ReportsPage/ 2>/dev/null
cp src/services/reportService.js src/features/reports/services/ 2>/dev/null

# Copy Team Feature
cp -r src/components/pages/TeamPage/* src/features/team/pages/TeamPage/ 2>/dev/null
cp -r src/components/molecules/AssignRoleDialog/* src/features/team/components/AssignRoleDialog/ 2>/dev/null
cp src/services/userService.js src/features/team/services/ 2>/dev/null

echo "✓ All files copied to feature folders"
echo "Migration complete! Next step: Create index.js files for each feature"
