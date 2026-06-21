-- Migration Script: Add Schedule-Based Seat Availability
-- This script creates the ScheduleSeat table and migrates existing data

-- Step 1: Create ScheduleSeat table
CREATE TABLE IF NOT EXISTS "ScheduleSeat" (
    id BIGSERIAL PRIMARY KEY,
    "scheduleId" BIGINT NOT NULL,
    "seatId" BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    "bookingId" BIGINT,
    
    CONSTRAINT fk_scheduleseat_schedule FOREIGN KEY ("scheduleId") 
        REFERENCES "BusSchedule"(id) ON DELETE CASCADE,
    CONSTRAINT fk_scheduleseat_seat FOREIGN KEY ("seatId") 
        REFERENCES "Seat"(id) ON DELETE CASCADE,
    CONSTRAINT uk_schedule_seat UNIQUE ("scheduleId", "seatId")
);

-- Step 2: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_scheduleseat_schedule ON "ScheduleSeat"("scheduleId");
CREATE INDEX IF NOT EXISTS idx_scheduleseat_seat ON "ScheduleSeat"("seatId");
CREATE INDEX IF NOT EXISTS idx_scheduleseat_status ON "ScheduleSeat"(status);
CREATE INDEX IF NOT EXISTS idx_scheduleseat_schedule_status ON "ScheduleSeat"("scheduleId", status);
CREATE INDEX IF NOT EXISTS idx_scheduleseat_booking ON "ScheduleSeat"("bookingId");

-- Step 3: Populate ScheduleSeat for all existing schedules
-- This creates a ScheduleSeat record for each combination of schedule and seat
-- All seats start as AVAILABLE for each schedule
INSERT INTO "ScheduleSeat" ("scheduleId", "seatId", status, "bookingId")
SELECT 
    s.id AS "scheduleId",
    seat.id AS "seatId",
    'AVAILABLE' AS status,
    NULL AS "bookingId"
FROM "BusSchedule" s
CROSS JOIN "Seat" seat
WHERE seat."busId" = s."busId"
ON CONFLICT ("scheduleId", "seatId") DO NOTHING;

-- Step 4: Update ScheduleSeat with booking information from BookingSeat
-- This links existing bookings to their schedule seats
UPDATE "ScheduleSeat" ss
SET 
    status = 'BOOKED',
    "bookingId" = b.id
FROM "Booking" b
INNER JOIN "BookingSeat" bs ON bs."bookingId" = b.id
WHERE ss."scheduleId" = b."scheduleId"
  AND ss."seatId" = bs."seatId"
  AND b."bookingStatus" IN ('PENDING', 'CONFIRMED')
  AND b."isDeleted" = false;

-- Step 5: Drop the status and bookings columns from Seat table
-- Status is now tracked per-schedule in ScheduleSeat table
ALTER TABLE "Seat" DROP COLUMN IF EXISTS status;
ALTER TABLE "Seat" DROP COLUMN IF EXISTS bookings;

-- Drop the old indexes that referenced status
DROP INDEX IF EXISTS idx_seat_status;
DROP INDEX IF EXISTS idx_seat_bus_status;

-- Step 6: Verify migration
-- Check counts to ensure data integrity
DO $$
DECLARE
    schedule_count INTEGER;
    seat_count INTEGER;
    scheduleseat_count INTEGER;
    expected_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO schedule_count FROM "BusSchedule";
    SELECT COUNT(*) INTO seat_count FROM "Seat";
    SELECT COUNT(*) INTO scheduleseat_count FROM "ScheduleSeat";
    
    -- Calculate expected count (schedules * average seats per bus)
    SELECT COUNT(*) INTO expected_count 
    FROM "BusSchedule" s
    INNER JOIN "Seat" seat ON seat."busId" = s."busId";
    
    RAISE NOTICE 'Migration Summary:';
    RAISE NOTICE '  Total Schedules: %', schedule_count;
    RAISE NOTICE '  Total Seats: %', seat_count;
    RAISE NOTICE '  Total ScheduleSeats Created: %', scheduleseat_count;
    RAISE NOTICE '  Expected ScheduleSeats: %', expected_count;
    
    IF scheduleseat_count = expected_count THEN
        RAISE NOTICE 'Migration completed successfully!';
    ELSE
        RAISE WARNING 'Migration count mismatch! Please verify data.';
    END IF;
END $$;

-- Step 7: Add comments to clarify the new structure
COMMENT ON TABLE "ScheduleSeat" IS 'Tracks seat availability per schedule. Each record represents a seat for a specific schedule.';
COMMENT ON COLUMN "ScheduleSeat".status IS 'Seat availability status for this specific schedule (AVAILABLE/BOOKED/UNAVAILABLE).';
COMMENT ON COLUMN "ScheduleSeat"."bookingId" IS 'Reference to the booking that reserved this seat for this schedule. NULL if available.';
COMMENT ON TABLE "Seat" IS 'Physical seats on a bus. Availability is tracked per-schedule in ScheduleSeat table.';
