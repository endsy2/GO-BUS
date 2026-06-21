-- -----------------------------------------------------------------------------
-- 1. PERMISSIONS
-- -----------------------------------------------------------------------------
INSERT INTO user_service."permission" (id, name, description) VALUES
(1,  'USER_READ',          'Read any user profile (admin)'),
(2,  'USER_WRITE',         'Update any user (admin)'),
(3,  'USER_DELETE',        'Delete any user (admin)'),
(4,  'WALLET_READ',        'Read own wallet & transactions'),
(5,  'WALLET_WRITE',       'Top-up / modify wallet'),
(6,  'NOTIFICATION_READ',  'Read own notifications'),
(7,  'NOTIFICATION_WRITE', 'Mark notifications as read'),
(8,  'BUS_READ',           'Read buses, routes, schedules, seats, layouts'),
(9,  'BUS_WRITE',          'Create / update buses, routes, schedules, seats, layouts'),
(10, 'BUS_DELETE',         'Delete buses, routes, schedules, seats, layouts'),
(11, 'BOOKING_READ',       'Read own bookings'),
(12, 'BOOKING_WRITE',      'Create / update bookings'),
(13, 'BOOKING_DELETE',     'Cancel a booking'),
(14, 'PAYMENT_READ',       'Read payments'),
(15, 'PAYMENT_WRITE',      'Initiate a payment'),
(16, 'TICKET_READ',        'View tickets'),
(17, 'PROMO_READ',         'Read promo codes'),
(18, 'PROMO_MANAGE',       'Create / update / delete promo codes'),
(19, 'ADMIN_ACCESS',       'Unrestricted admin access');

-- -----------------------------------------------------------------------------
-- 2. ROLES
-- -----------------------------------------------------------------------------
INSERT INTO user_service."role" (id, name, description, "created_at") VALUES
(1, 'role_USER',         'Standard authenticated user',             NOW()),
(2, 'role_BUS_OPERATOR', 'Bus operator — manages fleet & routes',   NOW()),
(3, 'role_ADMIN',        'Full administrator — unrestricted access', NOW());


-- -----------------------------------------------------------------------------
-- 3. ROLE ↔ PERMISSION MAPPINGS
-- -----------------------------------------------------------------------------

-- role_USER
INSERT INTO user_service."role_permission" ("role_id", "permission_id") VALUES
(1,4),(1,5),(1,6),(1,7),(1,8),
(1,11),(1,12),(1,13),
(1,14),(1,15),
(1,16),(1,17);

-- role_BUS_OPERATOR
INSERT INTO user_service."role_permission" ("role_id", "permission_id") VALUES
(2,8),(2,9),(2,10),
(2,11),
(2,14),
(2,16),(2,17),(2,18);

-- role_ADMIN (ALL)
INSERT INTO user_service."role_permission" ("role_id", "permission_id") VALUES
(3,1),(3,2),(3,3),
(3,4),(3,5),
(3,6),(3,7),
(3,8),(3,9),(3,10),
(3,11),(3,12),(3,13),
(3,14),(3,15),
(3,16),(3,17),(3,18),
(3,19);