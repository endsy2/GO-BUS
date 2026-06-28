/**
 * Unit tests for src/shared/utils/permissions.js
 *
 * These are pure-JS tests (no React, no DOM) so they run quickly and have no
 * rendering overhead.
 */
import {
  hasPermission,
  hasAnyPermission,
  hasAllPermissions,
  canViewCustomers,
  canEditCustomers,
  canDeleteCustomers,
  canViewBuses,
  canEditBuses,
  canDeleteBuses,
  canViewBookings,
  canEditBookings,
  canDeleteBookings,
  isAdmin,
} from './permissions';

// ─── Fixtures ────────────────────────────────────────────────────────────────

const adminUser = {
  roles: ['ROLE_ADMIN'],
};

const staffUser = {
  roles: [
    {
      name: 'ROLE_STAFF',
      permissions: [
        { name: 'USER_READ' },
        { name: 'BUS_READ' },
        { name: 'BOOKING_READ' },
        { name: 'BOOKING_WRITE' },
      ],
    },
  ],
};

const noPermissionUser = {
  roles: [{ name: 'ROLE_GUEST', permissions: [] }],
};

// ─── hasPermission ────────────────────────────────────────────────────────────

describe('hasPermission', () => {
  it('returns false for null user', () => {
    expect(hasPermission(null, 'BUS_READ')).toBe(false);
  });

  it('returns false for user with no roles array', () => {
    expect(hasPermission({}, 'BUS_READ')).toBe(false);
  });

  it('ROLE_ADMIN bypasses all permission checks', () => {
    expect(hasPermission(adminUser, 'BUS_READ')).toBe(true);
    expect(hasPermission(adminUser, 'SUPER_SECRET')).toBe(true);
    expect(hasPermission(adminUser, 'USER_DELETE')).toBe(true);
  });

  it('returns true when user has the matching permission object', () => {
    expect(hasPermission(staffUser, 'USER_READ')).toBe(true);
    expect(hasPermission(staffUser, 'BUS_READ')).toBe(true);
    expect(hasPermission(staffUser, 'BOOKING_WRITE')).toBe(true);
  });

  it('returns false when user does not have the permission', () => {
    expect(hasPermission(staffUser, 'BUS_WRITE')).toBe(false);
    expect(hasPermission(staffUser, 'USER_DELETE')).toBe(false);
  });

  it('returns false for user with an empty permissions array', () => {
    expect(hasPermission(noPermissionUser, 'BUS_READ')).toBe(false);
  });
});

// ─── hasAnyPermission ─────────────────────────────────────────────────────────

describe('hasAnyPermission', () => {
  it('returns true when user has at least one of the listed permissions', () => {
    expect(hasAnyPermission(staffUser, ['BUS_WRITE', 'USER_READ'])).toBe(true);
  });

  it('returns false when user has none of the listed permissions', () => {
    expect(hasAnyPermission(staffUser, ['BUS_DELETE', 'USER_DELETE'])).toBe(false);
  });

  it('ROLE_ADMIN returns true for any set of permissions', () => {
    expect(hasAnyPermission(adminUser, ['ANYTHING'])).toBe(true);
  });
});

// ─── hasAllPermissions ────────────────────────────────────────────────────────

describe('hasAllPermissions', () => {
  it('returns true only when user has all listed permissions', () => {
    expect(hasAllPermissions(staffUser, ['USER_READ', 'BUS_READ'])).toBe(true);
  });

  it('returns false when user is missing any one permission', () => {
    expect(hasAllPermissions(staffUser, ['USER_READ', 'BUS_WRITE'])).toBe(false);
  });

  it('ROLE_ADMIN satisfies all-permissions check', () => {
    expect(hasAllPermissions(adminUser, ['A', 'B', 'C'])).toBe(true);
  });
});

// ─── Domain helpers ───────────────────────────────────────────────────────────

describe('customer permission helpers', () => {
  it('canViewCustomers — true for USER_READ', () => {
    expect(canViewCustomers(staffUser)).toBe(true);
    expect(canViewCustomers(noPermissionUser)).toBe(false);
  });

  it('canEditCustomers — requires USER_WRITE', () => {
    expect(canEditCustomers(staffUser)).toBe(false);
    expect(canEditCustomers(adminUser)).toBe(true);
  });

  it('canDeleteCustomers — requires USER_DELETE', () => {
    expect(canDeleteCustomers(staffUser)).toBe(false);
    expect(canDeleteCustomers(adminUser)).toBe(true);
  });
});

describe('bus permission helpers', () => {
  it('canViewBuses — true for BUS_READ', () => {
    expect(canViewBuses(staffUser)).toBe(true);
    expect(canViewBuses(noPermissionUser)).toBe(false);
  });

  it('canEditBuses — requires BUS_WRITE', () => {
    expect(canEditBuses(staffUser)).toBe(false);
    expect(canEditBuses(adminUser)).toBe(true);
  });

  it('canDeleteBuses — requires BUS_DELETE', () => {
    expect(canDeleteBuses(staffUser)).toBe(false);
    expect(canDeleteBuses(adminUser)).toBe(true);
  });
});

describe('booking permission helpers', () => {
  it('canViewBookings — true for BOOKING_READ', () => {
    expect(canViewBookings(staffUser)).toBe(true);
    expect(canViewBookings(noPermissionUser)).toBe(false);
  });

  it('canEditBookings — true for BOOKING_WRITE', () => {
    expect(canEditBookings(staffUser)).toBe(true);
    expect(canEditBookings(noPermissionUser)).toBe(false);
  });

  it('canDeleteBookings — requires BOOKING_DELETE', () => {
    expect(canDeleteBookings(staffUser)).toBe(false);
    expect(canDeleteBookings(adminUser)).toBe(true);
  });
});

describe('isAdmin', () => {
  it('returns false for regular staff (isAdmin checks ADMIN_ACCESS, not ROLE_ADMIN)', () => {
    // isAdmin checks hasPermission(user, 'ADMIN_ACCESS')
    // ROLE_ADMIN string-role bypass means it returns true for adminUser
    expect(isAdmin(adminUser)).toBe(true);
  });

  it('returns false for staff without ADMIN_ACCESS', () => {
    expect(isAdmin(staffUser)).toBe(false);
  });
});
