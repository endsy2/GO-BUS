-- Rollback Script: Remove Schedule-Based Seat Availability
-- This script removes the ScheduleSeat table and restores the old system

-- Step 1: Backup ScheduleSeat data (optional, for safety)
-- CREATE TABLE "ScheduleSeat_backup" AS SELECT * FROM "ScheduleSeat";

-- Step 2: Drop foreign key constraints first
ALTER TABLE "ScheduleSeat" DROP CONSTRAINT IF EXISTS fk_scheduleseat_schedule;
ALTER TABLE "ScheduleSeat" DROP CONSTRAINT IF EXISTS fk_scheduleseat_seat;

-- Step 3: Drop indexes
DROP INDEX IF EXISTS idx_scheduleseat_schedule;
DROP INDEX IF EXISTS idx_scheduleseat_seat;
DROP INDEX IF EXISTS idx_scheduleseat_status;
DROP INDEX IF EXISTS idx_scheduleseat_schedule_status;
DROP INDEX IF EXISTS idx_scheduleseat_booking;

-- Step 4: Drop the ScheduleSeat table
DROP TABLE IF EXISTS "ScheduleSeat";

-- Step 5: Restore status and bookings columns to Seat table
ALTER TABLE "Seat" ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'AVAILABLE';
ALTER TABLE "Seat" ADD COLUMN IF NOT EXISTS bookings JSONB DEFAULT '[]'::jsonb;

-- Recreate indexes
CREATE INDEX IF NOT EXISTS idx_seat_status ON "Seat"(status);
CREATE INDEX IF NOT EXISTS idx_seat_bus_status ON "Seat"("busId", status);

-- Step 6: Verify rollback
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'ScheduleSeat') THEN
        RAISE NOTICE 'Rollback completed successfully! ScheduleSeat table removed.';
    ELSE
        RAISE WARNING 'Rollback failed! ScheduleSeat table still exists.';
    END IF;
END $$;
