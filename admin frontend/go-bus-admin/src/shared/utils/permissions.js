// Permission utility functions
export const hasPermission = (user, permissionName) => {
  if (!user || !user.roles) return false;
  
  return user.roles.some(role => {
    if (typeof role === 'string') {
      if (role === 'ROLE_ADMIN') return true;
      return false;
    }
    
    return role.permissions?.some(permission => 
      permission.name === permissionName || permission === permissionName
    );
  });
};

export const hasAnyPermission = (user, permissionNames) => {
  return permissionNames.some(permission => hasPermission(user, permission));
};

export const hasAllPermissions = (user, permissionNames) => {
  return permissionNames.every(permission => hasPermission(user, permission));
};

// Customer-specific permission checks
export const canViewCustomers = (user) => hasPermission(user, 'USER_READ');
export const canEditCustomers = (user) => hasPermission(user, 'USER_WRITE');
export const canDeleteCustomers = (user) => hasPermission(user, 'USER_DELETE');
export const canCreateCustomers = (user) => hasPermission(user, 'USER_WRITE');

// Bus-specific permission checks
export const canViewBuses = (user) => hasPermission(user, 'BUS_READ');
export const canEditBuses = (user) => hasPermission(user, 'BUS_WRITE');
export const canDeleteBuses = (user) => hasPermission(user, 'BUS_DELETE');
export const canCreateBuses = (user) => hasPermission(user, 'BUS_WRITE');

// Booking-specific permission checks
export const canViewBookings = (user) => hasPermission(user, 'BOOKING_READ');
export const canEditBookings = (user) => hasPermission(user, 'BOOKING_WRITE');
export const canDeleteBookings = (user) => hasPermission(user, 'BOOKING_DELETE');

// Admin access check
export const isAdmin = (user) => hasPermission(user, 'ADMIN_ACCESS');
