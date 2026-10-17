-- =============================================================================
-- Roles & Permissions Seed (schema: user_service)
-- =============================================================================
-- Optional. user-service's DataInitializer already creates every permission
-- below and grants all of them to ROLE_ADMIN on startup, so the default admin
-- works without this script. Run it to add ROLE_USER and ROLE_BUS_OPERATOR.
--
-- When: AFTER user-service has started once (Hibernate creates the tables).
--   psql -v ON_ERROR_STOP=1 -h localhost -U postgres -d busbooking -f seed_roles_permissions.sql
--
-- Idempotent: rows are matched by name (no hard-coded ids), so it is safe to
-- run any number of times and never clashes with rows DataInitializer created.
--
-- Afterwards, restart user-service (it clears the role/permission cache on
-- startup) so logins pick up the new mappings.
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. PERMISSIONS  (keep in sync with DataInitializer and RouteAuthorizationFilter)
-- -----------------------------------------------------------------------------
INSERT INTO user_service.permission (name, description) VALUES
('USER_READ',          'Read any user profile (admin)'),
('USER_WRITE',         'Update any user (admin)'),
('USER_DELETE',        'Delete any user (admin)'),
('WALLET_READ',        'Read own wallet & transactions'),
('WALLET_WRITE',       'Top-up / modify wallet'),
('NOTIFICATION_READ',  'Read own notifications'),
('NOTIFICATION_WRITE', 'Mark notifications as read'),
('BUS_READ',           'Read buses, routes, schedules, seats, layouts'),
('BUS_WRITE',          'Create / update buses, routes, schedules, seats, layouts'),
('BUS_DELETE',         'Delete buses, routes, schedules, seats, layouts'),
('BOOKING_READ',       'Read own bookings'),
('BOOKING_WRITE',      'Create / update bookings'),
('BOOKING_DELETE',     'Cancel a booking'),
('PAYMENT_READ',       'Read payments'),
('PAYMENT_WRITE',      'Initiate a payment'),
('TICKET_READ',        'View tickets'),
('PROMO_READ',         'Read promo codes'),
('PROMO_MANAGE',       'Create / update / delete promo codes'),
('ADMIN_ACCESS',       'Unrestricted admin access')
ON CONFLICT (name) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. ROLES
-- -----------------------------------------------------------------------------
INSERT INTO user_service.role (name, description, created_at) VALUES
('ROLE_USER',         'Standard authenticated user',                NOW()),
('ROLE_BUS_OPERATOR', 'Bus operator — manages fleet & routes',      NOW()),
('ROLE_ADMIN',        'Administrator role with full system access', NOW())
ON CONFLICT (name) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. ROLE ↔ PERMISSION MAPPINGS
-- -----------------------------------------------------------------------------
INSERT INTO user_service.role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM (VALUES
    -- ROLE_USER
    ('ROLE_USER', 'WALLET_READ'),
    ('ROLE_USER', 'WALLET_WRITE'),
    ('ROLE_USER', 'NOTIFICATION_READ'),
    ('ROLE_USER', 'NOTIFICATION_WRITE'),
    ('ROLE_USER', 'BUS_READ'),
    ('ROLE_USER', 'BOOKING_READ'),
    ('ROLE_USER', 'BOOKING_WRITE'),
    ('ROLE_USER', 'BOOKING_DELETE'),
    ('ROLE_USER', 'PAYMENT_READ'),
    ('ROLE_USER', 'PAYMENT_WRITE'),
    ('ROLE_USER', 'TICKET_READ'),
    ('ROLE_USER', 'PROMO_READ'),
    -- ROLE_BUS_OPERATOR
    ('ROLE_BUS_OPERATOR', 'BUS_READ'),
    ('ROLE_BUS_OPERATOR', 'BUS_WRITE'),
    ('ROLE_BUS_OPERATOR', 'BUS_DELETE'),
    ('ROLE_BUS_OPERATOR', 'BOOKING_READ'),
    ('ROLE_BUS_OPERATOR', 'PAYMENT_READ'),
    ('ROLE_BUS_OPERATOR', 'TICKET_READ'),
    ('ROLE_BUS_OPERATOR', 'PROMO_READ'),
    ('ROLE_BUS_OPERATOR', 'PROMO_MANAGE')
) AS m(role_name, permission_name)
JOIN user_service.role       r ON r.name = m.role_name
JOIN user_service.permission p ON p.name = m.permission_name
ON CONFLICT DO NOTHING;

-- ROLE_ADMIN (ALL)
INSERT INTO user_service.role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM user_service.role r
CROSS JOIN user_service.permission p
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

COMMIT;
