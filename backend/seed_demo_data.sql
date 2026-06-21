-- ============================================================================
-- Demo / Production-Like Seed Data — Cambodia Bus Booking
-- ============================================================================
-- Target schemas : user_service, bus_service, booking_service
-- Theme          : Cambodia (real cities, Khmer names, real operators)
-- Currency       : USD (primary for intercity bus fares in Cambodia)
-- Reference date : 2026-05-26
-- Demo password  : "password"  (BCrypt hash $2a$10$N9qo8uLO...)
--
-- Prerequisites:
--   * Run the Spring Boot services at least once so Hibernate creates the
--     tables under schemas user_service / bus_service / booking_service.
--   * Run this script with a Postgres role that has access to all 3 schemas.
--
-- Usage:
--   psql -h <host> -U <user> -d <db> -f seed_demo_data.sql
-- ============================================================================

BEGIN;

-- ============================================================================
-- 0. CLEAN UP  (safe re-runs)
-- ============================================================================
TRUNCATE TABLE booking_service."PromoUsage"   RESTART IDENTITY CASCADE;
TRUNCATE TABLE booking_service."Refund"       RESTART IDENTITY CASCADE;
TRUNCATE TABLE booking_service."Ticket"       RESTART IDENTITY CASCADE;
TRUNCATE TABLE booking_service."Payment"      RESTART IDENTITY CASCADE;
TRUNCATE TABLE booking_service."BookingSeat"  RESTART IDENTITY CASCADE;
TRUNCATE TABLE booking_service."Booking"      RESTART IDENTITY CASCADE;
TRUNCATE TABLE booking_service."PromoCode"    RESTART IDENTITY CASCADE;

TRUNCATE TABLE bus_service."ScheduleSeat"     RESTART IDENTITY CASCADE;
TRUNCATE TABLE bus_service."BusSchedule"      RESTART IDENTITY CASCADE;
TRUNCATE TABLE bus_service."Seat"             RESTART IDENTITY CASCADE;
TRUNCATE TABLE bus_service."Bus"              RESTART IDENTITY CASCADE;
TRUNCATE TABLE bus_service."BusRoute"         RESTART IDENTITY CASCADE;
TRUNCATE TABLE bus_service."BusLayout"        RESTART IDENTITY CASCADE;

TRUNCATE TABLE user_service."WalletTransaction" RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_service."Notification"      RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_service."TopUp"             RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_service."UserWallet"        CASCADE;
TRUNCATE TABLE user_service."UserPreference"    RESTART IDENTITY CASCADE;
DELETE FROM user_service."UserRole";
DELETE FROM user_service."RolePermission";
TRUNCATE TABLE user_service."User"           RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_service."Role"           RESTART IDENTITY CASCADE;
TRUNCATE TABLE user_service."Permission"     RESTART IDENTITY CASCADE;


-- ============================================================================
-- 1. PERMISSIONS
-- ============================================================================
INSERT INTO user_service."Permission" (id, name, description) VALUES
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


-- ============================================================================
-- 2. ROLES
-- ============================================================================
INSERT INTO user_service."Role" (id, name, description, "createdAt") VALUES
(1, 'ROLE_USER',         'Standard authenticated user',              NOW()),
(2, 'ROLE_BUS_OPERATOR', 'Bus operator — manages fleet & routes',    NOW()),
(3, 'ROLE_ADMIN',        'Full administrator — unrestricted access', NOW());

-- Role <-> Permission mappings
INSERT INTO user_service."RolePermission" ("roleId", "permissionId") VALUES
-- ROLE_USER
(1,4),(1,5),(1,6),(1,7),(1,8),(1,11),(1,12),(1,13),(1,14),(1,15),(1,16),(1,17),
-- ROLE_BUS_OPERATOR
(2,8),(2,9),(2,10),(2,11),(2,14),(2,16),(2,17),(2,18),
-- ROLE_ADMIN (everything)
(3,1),(3,2),(3,3),(3,4),(3,5),(3,6),(3,7),(3,8),(3,9),(3,10),
(3,11),(3,12),(3,13),(3,14),(3,15),(3,16),(3,17),(3,18),(3,19);


-- ============================================================================
-- 3. USERS  (10 Cambodian users — 1 admin, 1 operator, 8 customers)
-- ============================================================================
-- Password for ALL accounts = "password"
-- BCrypt-10 hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO user_service."User"
    (id, "userName", "fullName", email, phone, "passwordHash", image, gender,
     active, "isEmployee", "isDeleted", "isWalletExist", "createdAt", "updatedAt")
VALUES
(1, 'admin',          'Admin Sophea',          'admin@gobus.kh',           '+85512000001',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, TRUE, FALSE, TRUE, '2026-01-05 09:00:00', NOW()),

(2, 'operator_ibis',  'Chan Dara',             'operator@giantibis.kh',    '+85512000002',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, TRUE, FALSE, TRUE, '2026-01-10 09:00:00', NOW()),

(3, 'sok_pisey',      'Sok Pisey',             'pisey.sok@gmail.com',      '+85578123456',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'FEMALE',
    TRUE, FALSE, FALSE, TRUE, '2026-02-12 14:23:11', NOW()),

(4, 'ly_sothea',      'Ly Sothea',             'ly.sothea@yahoo.com',      '+85596445522',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, FALSE, FALSE, TRUE, '2026-02-18 10:11:00', NOW()),

(5, 'heng_sopheak',   'Heng Sopheak',          'sopheak.heng@gmail.com',   '+85581998877',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, FALSE, FALSE, TRUE, '2026-02-25 08:42:30', NOW()),

(6, 'mom_chenda',     'Mom Chenda',            'chenda.mom@outlook.com',   '+85577334411',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'FEMALE',
    TRUE, FALSE, FALSE, TRUE, '2026-03-01 16:18:22', NOW()),

(7, 'phan_sreyleak',  'Phan Sreyleak',         'sreyleak.phan@gmail.com',  '+85569887766',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'FEMALE',
    TRUE, FALSE, FALSE, TRUE, '2026-03-08 11:05:00', NOW()),

(8, 'tep_bopha',      'Tep Bopha',             'bopha.tep@gmail.com',      '+85515556677',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'FEMALE',
    TRUE, FALSE, FALSE, TRUE, '2026-03-15 19:33:44', NOW()),

(9, 'kim_vannak',     'Kim Vannak',            'vannak.kim@gmail.com',     '+85586221133',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, FALSE, FALSE, TRUE, '2026-04-02 09:14:55', NOW()),

(10,'long_sokhom',    'Long Sokhom',           'sokhom.long@gmail.com',    '+85593776655',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, FALSE, FALSE, TRUE, '2026-04-10 13:00:00', NOW()),

(11,'mao_channary',   'Mao Channary',          'channary.mao@gmail.com',   '+85577445566',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'FEMALE',
    TRUE, FALSE, FALSE, TRUE, '2026-04-18 17:21:30', NOW()),

(12,'vong_sambath',   'Vong Sambath',          'sambath.vong@gmail.com',   '+85561223344',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, 'MALE',
    TRUE, FALSE, FALSE, TRUE, '2026-05-01 08:00:00', NOW());

-- User <-> Role assignments
INSERT INTO user_service."UserRole" ("userId", "roleId") VALUES
(1, 3),  -- admin       -> ROLE_ADMIN
(2, 2),  -- operator    -> ROLE_BUS_OPERATOR
(3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1), (11, 1), (12, 1);


-- ============================================================================
-- 4. USER PREFERENCES  (theme)
-- ============================================================================
INSERT INTO user_service."UserPreference" (id, "userId", theme) VALUES
(1, 1, 'DARK'),
(2, 2, 'LIGHT'),
(3, 3, 'LIGHT'),
(4, 4, 'DARK'),
(5, 5, 'LIGHT'),
(6, 6, 'LIGHT'),
(7, 7, 'DARK'),
(8, 8, 'LIGHT'),
(9, 9, 'DARK'),
(10,10,'LIGHT'),
(11,11,'LIGHT'),
(12,12,'DARK');


-- ============================================================================
-- 5. USER WALLETS  (UUID PK)
-- ============================================================================
INSERT INTO user_service."UserWallet"
    (id, "userId", balance, currency, status, "lastTransaction", "createdAt", "updatedAt", pin_code)
VALUES
('11111111-1111-1111-1111-000000000001', 1,  500.00, 'USD', 'ACTIVE', '2026-05-20 10:00:00', '2026-01-05 09:05:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000002', 2,  120.00, 'USD', 'ACTIVE', '2026-05-15 11:30:00', '2026-01-10 09:05:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000003', 3,  150.50, 'USD', 'ACTIVE', '2026-05-22 18:42:00', '2026-02-12 14:25:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000004', 4,   80.25, 'USD', 'ACTIVE', '2026-05-23 09:11:00', '2026-02-18 10:13:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000005', 5,  220.00, 'USD', 'ACTIVE', '2026-05-21 14:00:00', '2026-02-25 08:44:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000006', 6,   45.00, 'USD', 'ACTIVE', '2026-05-20 12:00:00', '2026-03-01 16:20:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000007', 7,   60.75, 'USD', 'ACTIVE', '2026-05-19 17:00:00', '2026-03-08 11:07:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000008', 8,  100.00, 'USD', 'ACTIVE', '2026-05-18 08:15:00', '2026-03-15 19:35:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000009', 9,   35.00, 'USD', 'ACTIVE', '2026-05-17 10:00:00', '2026-04-02 09:16:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000010',10,  175.50, 'USD', 'ACTIVE', '2026-05-22 20:30:00', '2026-04-10 13:02:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000011',11,   25.00, 'USD', 'ACTIVE', '2026-05-10 09:00:00', '2026-04-18 17:23:00', NOW(), '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
('11111111-1111-1111-1111-000000000012',12,    0.00, 'USD', 'ACTIVE', NULL,                  '2026-05-01 08:02:00', NOW(), NULL);


-- ============================================================================
-- 6. TOP UPS  (completed top-ups via Bakong / ABA / Wing)
-- ============================================================================
INSERT INTO user_service."TopUp"
    (id, "userId", amount, "paymentMethod", status, "paymentGateway", "transactionId", "completedAt", "createdAt")
VALUES
(1, 3, 200.00, 'BAKONG', 'COMPLETED', 'Bakong KHQR',    'BKG-202603-000123', '2026-03-15 10:32:00', '2026-03-15 10:30:00'),
(2, 3, 100.00, 'ABA',    'COMPLETED', 'ABA PayWay',     'ABA-202604-000456', '2026-04-10 09:45:00', '2026-04-10 09:43:00'),
(3, 4, 150.00, 'BAKONG', 'COMPLETED', 'Bakong KHQR',    'BKG-202603-000789', '2026-03-20 14:00:00', '2026-03-20 13:58:00'),
(4, 5, 300.00, 'WING',   'COMPLETED', 'Wing Cambodia',  'WNG-202604-001122', '2026-04-05 17:21:00', '2026-04-05 17:18:00'),
(5, 6, 100.00, 'BAKONG', 'COMPLETED', 'Bakong KHQR',    'BKG-202604-002233', '2026-04-15 11:00:00', '2026-04-15 10:58:00'),
(6, 7,  80.00, 'ABA',    'COMPLETED', 'ABA PayWay',     'ABA-202605-003344', '2026-05-02 09:30:00', '2026-05-02 09:27:00'),
(7, 8, 120.00, 'ACLEDA', 'COMPLETED', 'ACLEDA XPay',    'ACL-202605-004455', '2026-05-08 16:00:00', '2026-05-08 15:57:00'),
(8, 9,  50.00, 'BAKONG', 'COMPLETED', 'Bakong KHQR',    'BKG-202605-005566', '2026-05-12 12:00:00', '2026-05-12 11:58:00'),
(9, 10,200.00, 'BAKONG', 'COMPLETED', 'Bakong KHQR',    'BKG-202605-006677', '2026-05-19 20:15:00', '2026-05-19 20:12:00'),
(10,3,  50.00, 'BAKONG', 'PENDING',   'Bakong KHQR',    'BKG-202605-007788', NULL,                  '2026-05-26 08:00:00');


-- ============================================================================
-- 7. WALLET TRANSACTIONS
-- ============================================================================
INSERT INTO user_service."WalletTransaction"
    (id, "walletId", amount, type, status, "referenceId", description,
     "balanceBefore", "balanceAfter", metadata, "createdAt", "completedAt")
VALUES
(1, '11111111-1111-1111-1111-000000000003', 200.00, 'TOP_UP',  'COMPLETED', 'BKG-202603-000123', 'Top-up via Bakong KHQR',
    0.00, 200.00, '{"gateway":"Bakong","channel":"KHQR"}', '2026-03-15 10:32:00', '2026-03-15 10:32:00'),
(2, '11111111-1111-1111-1111-000000000003', 100.00, 'TOP_UP',  'COMPLETED', 'ABA-202604-000456', 'Top-up via ABA PayWay',
    200.00, 300.00, '{"gateway":"ABA"}', '2026-04-10 09:45:00', '2026-04-10 09:45:00'),
(3, '11111111-1111-1111-1111-000000000003', 14.00,  'PAYMENT', 'COMPLETED', 'BK-000001',         'Bus ticket: Phnom Penh -> Siem Reap',
    300.00, 286.00, NULL, '2026-04-20 09:12:00', '2026-04-20 09:12:00'),
(4, '11111111-1111-1111-1111-000000000003', 14.00,  'PAYMENT', 'COMPLETED', 'BK-000002',         'Bus ticket: Siem Reap -> Phnom Penh',
    286.00, 272.00, NULL, '2026-05-22 18:42:00', '2026-05-22 18:42:00'),
(5, '11111111-1111-1111-1111-000000000004', 150.00, 'TOP_UP',  'COMPLETED', 'BKG-202603-000789', 'Top-up via Bakong KHQR',
    0.00, 150.00, '{"gateway":"Bakong"}', '2026-03-20 14:00:00', '2026-03-20 14:00:00'),
(6, '11111111-1111-1111-1111-000000000004', 9.00,   'PAYMENT', 'COMPLETED', 'BK-000003',         'Bus ticket: Phnom Penh -> Sihanoukville',
    150.00, 141.00, NULL, '2026-05-23 09:11:00', '2026-05-23 09:11:00'),
(7, '11111111-1111-1111-1111-000000000005', 300.00, 'TOP_UP',  'COMPLETED', 'WNG-202604-001122', 'Top-up via Wing',
    0.00, 300.00, '{"gateway":"Wing"}', '2026-04-05 17:21:00', '2026-04-05 17:21:00'),
(8, '11111111-1111-1111-1111-000000000005', 30.00,  'PAYMENT', 'COMPLETED', 'BK-000004',         'Bus ticket: Phnom Penh -> Banlung',
    300.00, 270.00, NULL, '2026-05-21 14:00:00', '2026-05-21 14:00:00'),
(9, '11111111-1111-1111-1111-000000000005', 50.00,  'BONUS',   'COMPLETED', 'PROMO-WELCOME50',   'Welcome bonus',
    270.00, 320.00, '{"promo":"WELCOME50"}', '2026-04-06 10:00:00', '2026-04-06 10:00:00'),
(10,'11111111-1111-1111-1111-000000000010', 200.00, 'TOP_UP',  'COMPLETED', 'BKG-202605-006677', 'Top-up via Bakong KHQR',
    0.00, 200.00, '{"gateway":"Bakong"}', '2026-05-19 20:15:00', '2026-05-19 20:15:00'),
(11,'11111111-1111-1111-1111-000000000010', 24.50,  'PAYMENT', 'COMPLETED', 'BK-000005',         'Bus ticket: Phnom Penh -> Battambang',
    200.00, 175.50, NULL, '2026-05-22 20:30:00', '2026-05-22 20:30:00'),
(12,'11111111-1111-1111-1111-000000000006', 100.00, 'TOP_UP',  'COMPLETED', 'BKG-202604-002233', 'Top-up via Bakong',
    0.00, 100.00, NULL, '2026-04-15 11:00:00', '2026-04-15 11:00:00'),
(13,'11111111-1111-1111-1111-000000000006', 7.00,   'PAYMENT', 'COMPLETED', 'BK-000006',         'Bus ticket: Phnom Penh -> Kampot',
    100.00, 93.00, NULL, '2026-05-20 12:00:00', '2026-05-20 12:00:00'),
(14,'11111111-1111-1111-1111-000000000007',  80.00, 'TOP_UP',  'COMPLETED', 'ABA-202605-003344', 'Top-up via ABA',
    0.00, 80.00, NULL, '2026-05-02 09:30:00', '2026-05-02 09:30:00'),
(15,'11111111-1111-1111-1111-000000000008', 120.00, 'TOP_UP',  'COMPLETED', 'ACL-202605-004455', 'Top-up via ACLEDA',
    0.00, 120.00, NULL, '2026-05-08 16:00:00', '2026-05-08 16:00:00'),
(16,'11111111-1111-1111-1111-000000000003', 14.00,  'REFUND',  'COMPLETED', 'RF-000001',         'Refund for cancelled booking #6',
    272.00, 286.00, '{"reason":"trip cancelled by operator"}', '2026-05-24 10:00:00', '2026-05-24 10:00:00');


-- ============================================================================
-- 8. NOTIFICATIONS
-- ============================================================================
INSERT INTO user_service."Notification"
    (id, "userId", type, message, "isRead", "createdAt")
VALUES
(1, 3, 'TOP_UP_SUCCESS',         'Your wallet top-up of $200.00 was successful.',                                 TRUE,  '2026-03-15 10:32:00'),
(2, 3, 'BOOKING_CONFIRMATION',   'Booking #1 confirmed: Phnom Penh → Siem Reap, 25 May 2026 07:30.',              TRUE,  '2026-05-22 18:42:00'),
(3, 3, 'PAYMENT_SUCCESS',        'Payment of $14.00 processed for booking #1.',                                   TRUE,  '2026-05-22 18:42:00'),
(4, 4, 'BOOKING_CONFIRMATION',   'Booking #2 confirmed: Phnom Penh → Sihanoukville, 24 May 2026 08:00.',          TRUE,  '2026-05-23 09:11:00'),
(5, 4, 'BUS_REMINDER',           'Reminder: Your bus to Sihanoukville departs in 2 hours.',                       FALSE, '2026-05-24 06:00:00'),
(6, 5, 'WALLET_CREDIT',          'Welcome bonus of $50.00 credited to your wallet.',                              TRUE,  '2026-04-06 10:00:00'),
(7, 5, 'BOOKING_CONFIRMATION',   'Booking #4 confirmed: Phnom Penh → Banlung, 28 May 2026 19:30.',                FALSE, '2026-05-21 14:00:00'),
(8, 6, 'BOOKING_CONFIRMATION',   'Booking #6 confirmed: Phnom Penh → Kampot, 26 May 2026 13:00.',                 TRUE,  '2026-05-20 12:00:00'),
(9, 6, 'CANCELLATION',           'Your booking #6 was cancelled. Refund of $7.00 is being processed.',            FALSE, '2026-05-24 10:00:00'),
(10,7, 'TOP_UP_SUCCESS',         'Your wallet top-up of $80.00 was successful.',                                  TRUE,  '2026-05-02 09:30:00'),
(11,8, 'TOP_UP_SUCCESS',         'Your wallet top-up of $120.00 was successful.',                                 TRUE,  '2026-05-08 16:00:00'),
(12,10,'BOOKING_CONFIRMATION',   'Booking #5 confirmed: Phnom Penh → Battambang, 27 May 2026 08:00.',             FALSE, '2026-05-22 20:30:00'),
(13,9, 'LOW_BALANCE',            'Your wallet balance is below $50.00.',                                          FALSE, '2026-05-25 09:00:00'),
(14,11,'BOOKING_CONFIRMATION',   'Booking #8 confirmed: Siem Reap → Battambang, 30 May 2026 09:00.',              FALSE, '2026-05-25 11:00:00'),
(15,12,'WALLET_CREDIT',          'Welcome to GoBus! Use code WELCOME50 for 50% off your first ride.',             FALSE, '2026-05-01 08:02:00');


-- ============================================================================
-- 9. BUS LAYOUTS  (3 standard Cambodian intercity layouts)
-- ============================================================================
INSERT INTO bus_service."BusLayout" (id, name, layout, description, "createdAt")
VALUES
(1, 'Standard 40-Seat (2-2)',
    '[
       {"row":1,"seats":["1A","1B",null,"1C","1D"]},
       {"row":2,"seats":["2A","2B",null,"2C","2D"]},
       {"row":3,"seats":["3A","3B",null,"3C","3D"]},
       {"row":4,"seats":["4A","4B",null,"4C","4D"]},
       {"row":5,"seats":["5A","5B",null,"5C","5D"]},
       {"row":6,"seats":["6A","6B",null,"6C","6D"]},
       {"row":7,"seats":["7A","7B",null,"7C","7D"]},
       {"row":8,"seats":["8A","8B",null,"8C","8D"]},
       {"row":9,"seats":["9A","9B",null,"9C","9D"]},
       {"row":10,"seats":["10A","10B",null,"10C","10D"]}
     ]'::jsonb,
    'Standard 40-seat intercity coach (2-2 configuration). Used by most Cambodian operators on regular routes.',
    '2026-01-01 09:00:00'),

(2, 'VIP 36-Seat (2-1)',
    '[
       {"row":1,"seats":["1A","1B",null,"1C"]},
       {"row":2,"seats":["2A","2B",null,"2C"]},
       {"row":3,"seats":["3A","3B",null,"3C"]},
       {"row":4,"seats":["4A","4B",null,"4C"]},
       {"row":5,"seats":["5A","5B",null,"5C"]},
       {"row":6,"seats":["6A","6B",null,"6C"]},
       {"row":7,"seats":["7A","7B",null,"7C"]},
       {"row":8,"seats":["8A","8B",null,"8C"]},
       {"row":9,"seats":["9A","9B",null,"9C"]},
       {"row":10,"seats":["10A","10B",null,"10C"]},
       {"row":11,"seats":["11A","11B",null,"11C"]},
       {"row":12,"seats":["12A","12B",null,"12C"]}
     ]'::jsonb,
    'VIP 36-seat bus (2-1 configuration). Wider seats, USB ports, in-cabin Wi-Fi.',
    '2026-01-01 09:00:00'),

(3, 'Sleeper 24-Bed (1-1)',
    '[
       {"row":1,"seats":["1A",null,"1B"]},
       {"row":2,"seats":["2A",null,"2B"]},
       {"row":3,"seats":["3A",null,"3B"]},
       {"row":4,"seats":["4A",null,"4B"]},
       {"row":5,"seats":["5A",null,"5B"]},
       {"row":6,"seats":["6A",null,"6B"]},
       {"row":7,"seats":["7A",null,"7B"]},
       {"row":8,"seats":["8A",null,"8B"]},
       {"row":9,"seats":["9A",null,"9B"]},
       {"row":10,"seats":["10A",null,"10B"]},
       {"row":11,"seats":["11A",null,"11B"]},
       {"row":12,"seats":["12A",null,"12B"]}
     ]'::jsonb,
    'Sleeper bus with 24 single beds (1-1 configuration). For overnight long-haul routes.',
    '2026-01-01 09:00:00');


-- ============================================================================
-- 10. BUS ROUTES  (Cambodia intercity)
-- ============================================================================
INSERT INTO bus_service."BusRoute"
    (id, origin, destination, "distanceKm", "durationMinutes", "originLocation", "destinationLocation")
VALUES
(1, 'Phnom Penh',  'Siem Reap',     314.0, 360,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":13.3671,"lng":103.8448,"address":"Siem Reap Bus Terminal","province":"Siem Reap"}'::jsonb),

(2, 'Phnom Penh',  'Sihanoukville', 230.0, 270,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":10.6101,"lng":103.5290,"address":"Sihanoukville Bus Station","province":"Preah Sihanouk"}'::jsonb),

(3, 'Phnom Penh',  'Battambang',    291.0, 360,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":13.0957,"lng":103.2022,"address":"Battambang Central Market","province":"Battambang"}'::jsonb),

(4, 'Phnom Penh',  'Kampot',        148.0, 180,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":10.6101,"lng":104.1810,"address":"Kampot Old Bridge","province":"Kampot"}'::jsonb),

(5, 'Phnom Penh',  'Kep',           169.0, 210,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":10.5145,"lng":104.3151,"address":"Kep Beach","province":"Kep"}'::jsonb),

(6, 'Phnom Penh',  'Poipet',        407.0, 480,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":13.6606,"lng":102.5673,"address":"Poipet Border","province":"Banteay Meanchey"}'::jsonb),

(7, 'Phnom Penh',  'Kampong Cham',  124.0, 150,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":11.9920,"lng":105.4633,"address":"Kampong Cham Riverside","province":"Kampong Cham"}'::jsonb),

(8, 'Phnom Penh',  'Banlung',       590.0, 600,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":13.7395,"lng":106.9881,"address":"Banlung Town Center","province":"Ratanakiri"}'::jsonb),

(9, 'Siem Reap',   'Phnom Penh',    314.0, 360,
    '{"lat":13.3671,"lng":103.8448,"address":"Siem Reap Bus Terminal","province":"Siem Reap"}'::jsonb,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb),

(10,'Siem Reap',   'Battambang',    170.0, 210,
    '{"lat":13.3671,"lng":103.8448,"address":"Siem Reap Bus Terminal","province":"Siem Reap"}'::jsonb,
    '{"lat":13.0957,"lng":103.2022,"address":"Battambang Central Market","province":"Battambang"}'::jsonb),

(11,'Sihanoukville','Phnom Penh',   230.0, 270,
    '{"lat":10.6101,"lng":103.5290,"address":"Sihanoukville Bus Station","province":"Preah Sihanouk"}'::jsonb,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb),

(12,'Phnom Penh',  'Kratie',        315.0, 360,
    '{"lat":11.5564,"lng":104.9282,"address":"Central Station, Phnom Penh","province":"Phnom Penh"}'::jsonb,
    '{"lat":12.4881,"lng":106.0188,"address":"Kratie Riverside","province":"Kratie"}'::jsonb);


-- ============================================================================
-- 11. BUSES  (real Cambodian operators)
-- ============================================================================
INSERT INTO bus_service."Bus"
    (id, "routeId", "busNumber", plate, model, status, "busType", "totalSeats", "layoutId")
VALUES
(1,  1, 'GIANT-IBIS-001',     '2A-1234', 'Hyundai Universe',  'Active',     'SEATER',   36, 2),
(2,  1, 'MEKONG-EXPRESS-101', '2A-5678', 'Hyundai Universe',  'Active',     'SEATER',   40, 1),
(3,  2, 'VIRAK-BUNTHAM-201',  '2A-3344', 'Daewoo BX212S',     'Active',     'SLEEPER',  24, 3),
(4,  2, 'GIANT-IBIS-002',     '2A-7788', 'Hyundai Universe',  'Active',     'SEATER',   36, 2),
(5,  3, 'PSD-MEKONG-301',     '2A-9911', 'Hyundai County',    'InService',  'SEATER',   40, 1),
(6,  4, 'CAMBODIA-VIP-401',   '2A-4422', 'Toyota Hiace VIP',  'Active',     'SEATER',   36, 2),
(7,  5, 'LARRYTA-501',        '2A-6655', 'Hyundai Universe',  'Active',     'SEATER',   40, 1),
(8,  6, 'VIRAK-BUNTHAM-601',  '2A-8800', 'Daewoo BX212S',     'Active',     'SLEEPER',  24, 3),
(9,  7, 'BAYON-VIP-701',      '2A-1010', 'Hyundai County',    'Active',     'SEATER',   40, 1),
(10, 8, 'VIRAK-BUNTHAM-801',  '2A-1212', 'Daewoo BX212S',     'Active',     'SLEEPER',  24, 3),
(11, 9, 'GIANT-IBIS-003',     '2A-1313', 'Hyundai Universe',  'Active',     'SEATER',   36, 2),
(12,10, 'MEKONG-EXPRESS-102', '2A-1414', 'Hyundai Universe',  'Active',     'SEATER',   40, 1),
(13,11, 'GIANT-IBIS-004',     '2A-1515', 'Hyundai Universe',  'Standby',    'SEATER',   36, 2),
(14,12, 'PSD-MEKONG-302',     '2A-1616', 'Hyundai County',    'Maintenance','SEATER',   40, 1);


-- ============================================================================
-- 12. SEATS  (auto-generated per bus, matching the layout)
-- ============================================================================
-- Layout 1: 40 seats — 10 rows × {A,B,C,D}
-- Layout 2: 36 seats — 12 rows × {A,B,C}
-- Layout 3: 24 beds  — 12 rows × {A,B}
INSERT INTO bus_service."Seat" ("busId", "seatNumber", "seatType")
SELECT b.id,
       r.row_num || c.col,
       CASE WHEN b."busType" = 'SLEEPER' THEN 'SLEEPER' ELSE 'SEATER' END
FROM   bus_service."Bus" b
JOIN   bus_service."BusLayout" bl ON bl.id = b."layoutId"
CROSS JOIN LATERAL (
    SELECT generate_series(1, CASE bl.id WHEN 1 THEN 10 WHEN 2 THEN 12 WHEN 3 THEN 12 END) AS row_num
) r
CROSS JOIN LATERAL (
    SELECT unnest(
        CASE bl.id
            WHEN 1 THEN ARRAY['A','B','C','D']
            WHEN 2 THEN ARRAY['A','B','C']
            WHEN 3 THEN ARRAY['A','B']
        END
    ) AS col
) c
ORDER BY b.id, r.row_num, c.col;


-- ============================================================================
-- 13. BUS SCHEDULES  (mix of past & future for reporting / dashboards)
-- ============================================================================
-- All times in Asia/Phnom_Penh (UTC+7) but stored as LocalDateTime (no TZ)
INSERT INTO bus_service."BusSchedule"
    (id, "busId", price, "departureDateTime", "arrivalDateTime", "bookingIds")
VALUES
-- ---------- Past schedules (for reports) ----------
(1,  1, 14.00, '2026-05-20 07:30:00', '2026-05-20 13:30:00', '[]'::jsonb),
(2,  2, 12.00, '2026-05-20 08:00:00', '2026-05-20 14:00:00', '[]'::jsonb),
(3,  3, 13.00, '2026-05-21 21:00:00', '2026-05-22 01:30:00', '[]'::jsonb),
(4,  4,  9.00, '2026-05-23 08:00:00', '2026-05-23 12:30:00', '[]'::jsonb),
(5,  6,  7.00, '2026-05-26 13:00:00', '2026-05-26 16:00:00', '[]'::jsonb),
(6,  7,  8.00, '2026-05-22 09:00:00', '2026-05-22 12:30:00', '[]'::jsonb),
(7,  5, 12.00, '2026-05-22 08:00:00', '2026-05-22 14:00:00', '[]'::jsonb),
(8, 10, 30.00, '2026-05-21 19:30:00', '2026-05-22 05:30:00', '[]'::jsonb),
(9, 11, 14.00, '2026-05-22 18:00:00', '2026-05-23 00:00:00', '[]'::jsonb),
(10,12, 10.00, '2026-05-25 09:00:00', '2026-05-25 12:30:00', '[]'::jsonb),
-- ---------- Today (2026-05-26) ----------
(11, 1, 14.00, '2026-05-26 07:30:00', '2026-05-26 13:30:00', '[]'::jsonb),
(12, 2, 12.00, '2026-05-26 08:00:00', '2026-05-26 14:00:00', '[]'::jsonb),
(13, 3, 13.00, '2026-05-26 21:00:00', '2026-05-27 01:30:00', '[]'::jsonb),
(14, 4,  9.00, '2026-05-26 08:00:00', '2026-05-26 12:30:00', '[]'::jsonb),
(15, 7,  8.00, '2026-05-26 09:00:00', '2026-05-26 12:30:00', '[]'::jsonb),
-- ---------- Future schedules ----------
(16, 1, 14.00, '2026-05-27 07:30:00', '2026-05-27 13:30:00', '[]'::jsonb),
(17, 5, 12.00, '2026-05-27 08:00:00', '2026-05-27 14:00:00', '[]'::jsonb),
(18, 8, 16.00, '2026-05-27 18:00:00', '2026-05-28 02:00:00', '[]'::jsonb),
(19,10, 30.00, '2026-05-28 19:30:00', '2026-05-29 05:30:00', '[]'::jsonb),
(20, 9,  8.00, '2026-05-28 09:00:00', '2026-05-28 11:30:00', '[]'::jsonb),
(21, 2, 12.00, '2026-05-29 08:00:00', '2026-05-29 14:00:00', '[]'::jsonb),
(22,12, 10.00, '2026-05-30 09:00:00', '2026-05-30 12:30:00', '[]'::jsonb),
(23, 6,  7.00, '2026-05-30 13:00:00', '2026-05-30 16:00:00', '[]'::jsonb),
(24,11, 14.00, '2026-05-31 18:00:00', '2026-06-01 00:00:00', '[]'::jsonb);


-- ============================================================================
-- 14. SCHEDULE SEATS  (1 row per schedule × seat — all AVAILABLE)
-- ============================================================================
INSERT INTO bus_service."ScheduleSeat" ("scheduleId", "seatId", status, "bookingId", "pendingUserId", "pendingAt")
SELECT sch.id,
       s.id,
       'AVAILABLE',
       NULL,
       NULL,
       NULL
FROM   bus_service."BusSchedule" sch
JOIN   bus_service."Seat" s ON s."busId" = sch."busId";


-- ============================================================================
-- 15. PROMO CODES
-- ============================================================================
INSERT INTO booking_service."PromoCode"
    (id, code, description, "discountType", "discountValue", "maxUses", "usedCount",
     "validFrom", "validTo", status, "createdAt", "updatedAt")
VALUES
(1, 'WELCOME50',    'Welcome — 50% off your first ride (max $5)', 'PERCENTAGE', 50.0,  500, 12,
    '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE',   '2026-01-01 09:00:00', NOW()),
(2, 'PCHUMBEN2026', 'Pchum Ben holiday — flat $2 off',             'FIXED',       2.0, 1000, 45,
    '2026-09-01 00:00:00', '2026-10-15 23:59:59', 'ACTIVE',   '2026-08-15 09:00:00', NOW()),
(3, 'KHMERNEWYEAR', 'Khmer New Year — 25% off any route',          'PERCENTAGE', 25.0, 2000, 134,
    '2026-04-01 00:00:00', '2026-04-30 23:59:59', 'EXPIRED',  '2026-03-15 09:00:00', NOW()),
(4, 'STUDENT10',    'Students — 10% off (with valid ID)',          'PERCENTAGE', 10.0, NULL, 78,
    '2026-01-01 00:00:00', '2026-12-31 23:59:59', 'ACTIVE',   '2026-01-01 09:00:00', NOW()),
(5, 'SIEMREAP5',    '$5 off Phnom Penh -> Siem Reap',              'FIXED',       5.0, 300, 22,
    '2026-05-01 00:00:00', '2026-07-31 23:59:59', 'ACTIVE',   '2026-04-25 09:00:00', NOW()),
(6, 'SEATTEST',     'Internal test code (inactive)',               'PERCENTAGE',100.0,    1,  0,
    '2026-01-01 00:00:00', '2027-01-01 00:00:00', 'INACTIVE', '2026-01-01 09:00:00', NOW());


-- ============================================================================
-- 16. BOOKINGS
-- ============================================================================
-- userId references user_service."User".id
-- scheduleId references bus_service."BusSchedule".id
-- promoId nullable; references booking_service."PromoCode".id
INSERT INTO booking_service."Booking"
    (id, "userId", "scheduleId", "bookingStatus", "totalAmount", "promoId",
     "paymentStatus", "paymentMethod", "createdAt", "updatedAt",
     "isDeleted", "deletedAt", "departureAt", "phoneNumber")
VALUES
-- Past confirmed bookings ----------------------------------------------------
(1, 3,  1, 'CONFIRMED', 14.00, NULL, 'SUCCESS', 'WALLET', '2026-05-19 14:23:11', '2026-05-19 14:23:11', FALSE, NULL, '2026-05-20 07:30:00', '+85578123456'),
(2, 4,  2,  'CONFIRMED',  9.00, NULL, 'SUCCESS', 'WALLET', '2026-05-22 09:11:00', '2026-05-22 09:11:00', FALSE, NULL, '2026-05-20 08:00:00', '+85596445522'),
(3, 5,  3, 'CONFIRMED', 13.00, NULL, 'SUCCESS', 'BAKONG', '2026-05-21 14:00:00', '2026-05-21 14:00:00', FALSE, NULL, '2026-05-21 21:00:00', '+85581998877'),
(4, 5,  8, 'CONFIRMED', 30.00, NULL, 'SUCCESS', 'WALLET', '2026-05-21 14:00:00', '2026-05-21 14:00:00', FALSE, NULL, '2026-05-21 19:30:00', '+85581998877'),
(5, 10, 9, 'CONFIRMED', 14.00, NULL, 'SUCCESS', 'WALLET', '2026-05-22 20:30:00', '2026-05-22 20:30:00', FALSE, NULL, '2026-05-22 18:00:00', '+85593776655'),

-- Cancelled (refunded)
(6, 6,  5, 'REFUNDED',  7.00, NULL, 'REFUNDED','WALLET', '2026-05-20 11:55:00', '2026-05-24 10:00:00', FALSE, NULL, '2026-05-26 13:00:00', '+85577334411'),

-- Past confirmed with promo code applied
(7, 3,  9, 'CONFIRMED',  9.00,    1, 'SUCCESS', 'WALLET', '2026-04-20 09:12:00', '2026-04-20 09:12:00', FALSE, NULL, '2026-05-22 18:00:00', '+85578123456'),

-- Upcoming confirmed
(8, 11, 4,'CONFIRMED',  9.00, NULL, 'SUCCESS', 'BAKONG', '2026-05-23 09:11:00', '2026-05-23 09:11:00', FALSE, NULL, '2026-05-23 08:00:00', '+85577445566'),
(9, 7, 14, 'CONFIRMED',  9.00,    4, 'SUCCESS', 'WALLET', '2026-05-25 09:00:00', '2026-05-25 09:00:00', FALSE, NULL, '2026-05-26 08:00:00', '+85569887766'),
(10, 8,15, 'CONFIRMED',  8.00, NULL, 'SUCCESS', 'BAKONG', '2026-05-25 11:00:00', '2026-05-25 11:00:00', FALSE, NULL, '2026-05-26 09:00:00', '+85515556677'),
(11, 9,11, 'CONFIRMED', 14.00, NULL, 'SUCCESS', 'BAKONG', '2026-05-25 18:00:00', '2026-05-25 18:00:00', FALSE, NULL, '2026-05-26 07:30:00', '+85586221133'),
(12, 4,16, 'CONFIRMED',  9.00,    5, 'SUCCESS', 'WALLET', '2026-05-25 19:00:00', '2026-05-25 19:00:00', FALSE, NULL, '2026-05-27 07:30:00', '+85596445522'),
(13, 5,18, 'CONFIRMED', 16.00, NULL, 'SUCCESS', 'BAKONG', '2026-05-25 20:00:00', '2026-05-25 20:00:00', FALSE, NULL, '2026-05-27 18:00:00', '+85581998877'),
(14, 6,19, 'CONFIRMED', 30.00, NULL, 'SUCCESS', 'BAKONG', '2026-05-26 06:00:00', '2026-05-26 06:00:00', FALSE, NULL, '2026-05-28 19:30:00', '+85577334411'),
(15,10,22, 'CONFIRMED', 10.00, NULL, 'SUCCESS', 'WALLET', '2026-05-26 07:00:00', '2026-05-26 07:00:00', FALSE, NULL, '2026-05-30 09:00:00', '+85593776655'),

-- Pending payment (Bakong QR not yet paid)
(16,12,17, 'PENDING',   12.00, NULL, 'PENDING', 'BAKONG', '2026-05-26 08:00:00', '2026-05-26 08:00:00', FALSE, NULL, '2026-05-27 08:00:00', '+85561223344'),
(17, 7,21, 'PENDING',   12.00, NULL, 'PENDING', 'BAKONG', '2026-05-26 08:15:00', '2026-05-26 08:15:00', FALSE, NULL, '2026-05-29 08:00:00', '+85569887766'),

-- Cancelled (user-initiated)
(18,11,20, 'CANCELLED',  8.00, NULL, 'CANCELLED','WALLET','2026-05-25 12:00:00', '2026-05-25 14:00:00', FALSE, NULL, '2026-05-28 09:00:00', '+85577445566'),

-- Failed payment (Bakong timeout)
(19, 8,23, 'FAILED',     7.00, NULL, 'EXPIRED', 'BAKONG', '2026-05-26 09:00:00', '2026-05-26 09:06:00', FALSE, NULL, '2026-05-30 13:00:00', '+85515556677'),

-- Soft-deleted
(20, 9,24, 'CANCELLED', 14.00, NULL, 'CANCELLED','BAKONG','2026-05-25 17:00:00', '2026-05-25 17:30:00', TRUE, '2026-05-25 17:30:00', '2026-05-31 18:00:00', '+85586221133');


-- ============================================================================
-- 17. BOOKING SEATS  (which physical seats each booking holds)
-- ============================================================================
-- We pick seat IDs deterministically: first 1..N seats of the corresponding bus.
-- For each booking we just pick the first 1-2 seats of the bus from that schedule.
INSERT INTO booking_service."BookingSeat"
    (id, "bookingId", "seatId", "passengerNumber")
SELECT
    row_number() OVER (ORDER BY b.id, ps.passenger_no) AS id,
    b.id,
    (SELECT s.id
       FROM bus_service."Seat" s
       JOIN bus_service."BusSchedule" sch ON sch."busId" = s."busId"
      WHERE sch.id = b."scheduleId"
      ORDER BY s.id
      OFFSET (b.id * 2 + ps.passenger_no - 3) LIMIT 1)            AS "seatId",
    ps.passenger_no                                                AS "passengerNumber"
FROM booking_service."Booking" b
CROSS JOIN LATERAL (
    SELECT generate_series(1,
        CASE WHEN b.id IN (3,4,11,13,14,15) THEN 2 ELSE 1 END
    ) AS passenger_no
) ps
ORDER BY b.id, ps.passenger_no;


-- ============================================================================
-- 18. UPDATE SCHEDULE SEATS for non-pending confirmed bookings -> BOOKED
-- ============================================================================
UPDATE bus_service."ScheduleSeat" ss
SET    status      = 'BOOKED',
       "bookingId" = b.id
FROM   booking_service."Booking" b
JOIN   booking_service."BookingSeat" bs ON bs."bookingId" = b.id
WHERE  ss."scheduleId" = b."scheduleId"
  AND  ss."seatId"     = bs."seatId"
  AND  b."bookingStatus" IN ('CONFIRMED');

-- Mark pending bookings' seats as PENDING in schedule_seat
UPDATE bus_service."ScheduleSeat" ss
SET    status            = 'PENDING',
       "pendingUserId"   = b."userId",
       "pendingAt"       = b."createdAt"
FROM   booking_service."Booking" b
JOIN   booking_service."BookingSeat" bs ON bs."bookingId" = b.id
WHERE  ss."scheduleId" = b."scheduleId"
  AND  ss."seatId"     = bs."seatId"
  AND  b."bookingStatus" = 'PENDING';


-- ============================================================================
-- 19. PAYMENTS
-- ============================================================================
INSERT INTO booking_service."Payment"
    (id, "bookingId", amount, currency, method, "transactionId", status, description, "paidAt", "walletTransactionId")
VALUES
(1,  1, 14.00, 'USD', 'WALLET', 'WTX-202605-000001', 'SUCCESS',  'Wallet payment for booking #1',   '2026-05-19 14:23:15', '11111111-1111-1111-1111-000000000003'::uuid),
(2,  2,  9.00, 'USD', 'WALLET', 'WTX-202605-000002', 'SUCCESS',  'Wallet payment for booking #2',   '2026-05-22 09:11:05', '11111111-1111-1111-1111-000000000004'::uuid),
(3,  3, 13.00, 'USD', 'BAKONG', 'BKG-202605-100001', 'SUCCESS',  'Bakong KHQR payment for #3',      '2026-05-21 14:00:30', NULL),
(4,  4, 30.00, 'USD', 'WALLET', 'WTX-202605-000003', 'SUCCESS',  'Wallet payment for booking #4',   '2026-05-21 14:00:50', '11111111-1111-1111-1111-000000000005'::uuid),
(5,  5, 14.00, 'USD', 'WALLET', 'WTX-202605-000004', 'SUCCESS',  'Wallet payment for booking #5',   '2026-05-22 20:30:10', '11111111-1111-1111-1111-000000000010'::uuid),
(6,  6,  7.00, 'USD', 'WALLET', 'WTX-202605-000005', 'REFUNDED', 'Refunded — trip cancelled',       '2026-05-20 11:55:30', '11111111-1111-1111-1111-000000000006'::uuid),
(7,  7,  9.00, 'USD', 'WALLET', 'WTX-202604-000001', 'SUCCESS',  'Wallet payment for #7 (promo)',   '2026-04-20 09:12:30', '11111111-1111-1111-1111-000000000003'::uuid),
(8,  8,  9.00, 'USD', 'BAKONG', 'BKG-202605-100002', 'SUCCESS',  'Bakong KHQR payment for #8',      '2026-05-23 09:11:40', NULL),
(9,  9,  9.00, 'USD', 'WALLET', 'WTX-202605-000006', 'SUCCESS',  'Wallet payment for #9 (promo)',   '2026-05-25 09:00:20', '11111111-1111-1111-1111-000000000007'::uuid),
(10,10,  8.00, 'USD', 'BAKONG', 'BKG-202605-100003', 'SUCCESS',  'Bakong KHQR payment for #10',     '2026-05-25 11:00:30', NULL),
(11,11, 14.00, 'USD', 'BAKONG', 'BKG-202605-100004', 'SUCCESS',  'Bakong KHQR payment for #11',     '2026-05-25 18:00:25', NULL),
(12,12,  9.00, 'USD', 'WALLET', 'WTX-202605-000007', 'SUCCESS',  'Wallet payment for #12 (promo)',  '2026-05-25 19:00:15', '11111111-1111-1111-1111-000000000004'::uuid),
(13,13, 16.00, 'USD', 'BAKONG', 'BKG-202605-100005', 'SUCCESS',  'Bakong KHQR payment for #13',     '2026-05-25 20:00:35', NULL),
(14,14, 30.00, 'USD', 'BAKONG', 'BKG-202605-100006', 'SUCCESS',  'Bakong KHQR payment for #14',     '2026-05-26 06:00:45', NULL),
(15,15, 10.00, 'USD', 'WALLET', 'WTX-202605-000008', 'SUCCESS',  'Wallet payment for #15',          '2026-05-26 07:00:25', '11111111-1111-1111-1111-000000000010'::uuid),
(16,16, 12.00, 'USD', 'BAKONG', 'BKG-202605-100007', 'PENDING',  'Awaiting Bakong KHQR scan',       NULL,                  NULL),
(17,17, 12.00, 'USD', 'BAKONG', 'BKG-202605-100008', 'PENDING',  'Awaiting Bakong KHQR scan',       NULL,                  NULL),
(18,18,  8.00, 'USD', 'WALLET', 'WTX-202605-000009', 'CANCELLED','Cancelled by user',               NULL,                  NULL),
(19,19,  7.00, 'USD', 'BAKONG', 'BKG-202605-100009', 'EXPIRED',  'Bakong QR expired',               NULL,                  NULL),
(20,20, 14.00, 'USD', 'BAKONG', 'BKG-202605-100010', 'CANCELLED','User cancelled before pay',       NULL,                  NULL);


-- ============================================================================
-- 20. TICKETS  (issued for CONFIRMED bookings only)
-- ============================================================================
INSERT INTO booking_service."Ticket" (id, "bookingId", "qrCode", "issuedAt")
SELECT
    row_number() OVER (ORDER BY b.id),
    b.id,
    'GOBUS-TKT-' || lpad(b.id::text, 8, '0') || '-' || encode(gen_random_bytes(8), 'hex'),
    b."updatedAt"
FROM booking_service."Booking" b
WHERE b."bookingStatus" = 'CONFIRMED';


-- ============================================================================
-- 21. PROMO USAGE
-- ============================================================================
INSERT INTO booking_service."PromoUsage" (id, "promoId", "userId", "bookingId", "usedAt")
VALUES
(1, 1, 3,  7,  '2026-04-20 09:12:30'),
(2, 4, 7,  9,  '2026-05-25 09:00:00'),
(3, 5, 4, 12,  '2026-05-25 19:00:00');


-- ============================================================================
-- 22. REFUNDS
-- ============================================================================
INSERT INTO booking_service."Refund"
    (id, "bookingId", amount, reason, status, "adminNote", "processedBy", "processedAt", "createdAt", "updatedAt")
VALUES
(1, 6, 7.00, 'Trip cancelled by operator due to flooding on NR3', 'COMPLETED',
    'Approved & wallet credited',         1, '2026-05-24 10:00:00', '2026-05-23 18:00:00', '2026-05-24 10:00:00'),
(2,18, 8.00, 'Change of plan — passenger cancelled',              'APPROVED',
    'Approved within 24h cancellation policy', 1, '2026-05-25 14:00:00', '2026-05-25 13:30:00', '2026-05-25 14:00:00');


-- ============================================================================
-- 23. RESET IDENTITY SEQUENCES TO MAX(id) + 1
-- ============================================================================
SELECT setval('user_service."Permission_id_seq"',        (SELECT COALESCE(MAX(id),0) FROM user_service."Permission")        + 1, FALSE);
SELECT setval('user_service."Role_id_seq"',              (SELECT COALESCE(MAX(id),0) FROM user_service."Role")              + 1, FALSE);
SELECT setval('user_service."User_id_seq"',              (SELECT COALESCE(MAX(id),0) FROM user_service."User")              + 1, FALSE);
SELECT setval('user_service."UserPreference_id_seq"',    (SELECT COALESCE(MAX(id),0) FROM user_service."UserPreference")    + 1, FALSE);
SELECT setval('user_service."TopUp_id_seq"',             (SELECT COALESCE(MAX(id),0) FROM user_service."TopUp")             + 1, FALSE);
SELECT setval('user_service."WalletTransaction_id_seq"', (SELECT COALESCE(MAX(id),0) FROM user_service."WalletTransaction") + 1, FALSE);
SELECT setval('user_service."Notification_id_seq"',      (SELECT COALESCE(MAX(id),0) FROM user_service."Notification")      + 1, FALSE);

SELECT setval('bus_service."BusLayout_id_seq"',          (SELECT COALESCE(MAX(id),0) FROM bus_service."BusLayout")          + 1, FALSE);
SELECT setval('bus_service."BusRoute_id_seq"',           (SELECT COALESCE(MAX(id),0) FROM bus_service."BusRoute")           + 1, FALSE);
SELECT setval('bus_service."Bus_id_seq"',                (SELECT COALESCE(MAX(id),0) FROM bus_service."Bus")                + 1, FALSE);
SELECT setval('bus_service."Seat_id_seq"',               (SELECT COALESCE(MAX(id),0) FROM bus_service."Seat")               + 1, FALSE);
SELECT setval('bus_service."BusSchedule_id_seq"',        (SELECT COALESCE(MAX(id),0) FROM bus_service."BusSchedule")        + 1, FALSE);
SELECT setval('bus_service."ScheduleSeat_id_seq"',       (SELECT COALESCE(MAX(id),0) FROM bus_service."ScheduleSeat")       + 1, FALSE);

SELECT setval('booking_service."PromoCode_id_seq"',      (SELECT COALESCE(MAX(id),0) FROM booking_service."PromoCode")      + 1, FALSE);
SELECT setval('booking_service."Booking_id_seq"',        (SELECT COALESCE(MAX(id),0) FROM booking_service."Booking")        + 1, FALSE);
SELECT setval('booking_service."BookingSeat_id_seq"',    (SELECT COALESCE(MAX(id),0) FROM booking_service."BookingSeat")    + 1, FALSE);
SELECT setval('booking_service."Payment_id_seq"',        (SELECT COALESCE(MAX(id),0) FROM booking_service."Payment")        + 1, FALSE);
SELECT setval('booking_service."Ticket_id_seq"',         (SELECT COALESCE(MAX(id),0) FROM booking_service."Ticket")         + 1, FALSE);
SELECT setval('booking_service."Refund_id_seq"',         (SELECT COALESCE(MAX(id),0) FROM booking_service."Refund")         + 1, FALSE);
SELECT setval('booking_service."PromoUsage_id_seq"',     (SELECT COALESCE(MAX(id),0) FROM booking_service."PromoUsage")     + 1, FALSE);


COMMIT;

-- ============================================================================
-- DONE — Summary
-- ============================================================================
--   12 users     (1 admin, 1 bus operator, 10 customers — all pwd "password")
--   12 wallets   (with top-ups + transaction history)
--   10 top-ups,  16 wallet transactions,  15 notifications
--    3 bus layouts, 12 routes, 14 buses, ~440 seats
--   24 schedules (past + today + future), ~870 schedule-seats
--   20 bookings  (mixed CONFIRMED/PENDING/CANCELLED/FAILED/REFUNDED/soft-deleted)
--   20 payments,  ~15 tickets, 6 promo codes, 3 promo usages, 2 refunds
-- ============================================================================
