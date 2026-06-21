-- Migration Script: Add PENDING Status Support to ScheduleSeat
-- This script updates the database to support the PENDING seat status

-- Step 1: Add new columns for PENDING status tracking (if they don't exist)
ALTER TABLE "ScheduleSeat" 
ADD COLUMN IF NOT EXISTS "pendingUserId" BIGINT,
ADD COLUMN IF NOT EXISTS "pendingAt" TIMESTAMP;

-- Step 2: Drop existing check constraint if it exists
ALTER TABLE "ScheduleSeat" 
DROP CONSTRAINT IF EXISTS schedule_seat_status_check;

-- Step 3: Add updated check constraint that includes PENDING status
ALTER TABLE "ScheduleSeat"
ADD CONSTRAINT schedule_seat_status_check 
CHECK (status IN ('AVAILABLE', 'BOOKED', 'UNAVAILABLE', 'PENDING'));

-- Step 4: Create index for pending seat cleanup queries
CREATE INDEX IF NOT EXISTS idx_scheduleseat_pending 
ON "ScheduleSeat"(status, "pendingAt") 
WHERE status = 'PENDING';

-- Step 5: Add comments
COMMENT ON COLUMN "ScheduleSeat"."pendingUserId" IS 'User ID who temporarily selected this seat. Used for PENDING status.';
COMMENT ON COLUMN "ScheduleSeat"."pendingAt" IS 'Timestamp when seat was marked as PENDING. Used for auto-expiry.';
COMMENT ON COLUMN "ScheduleSeat".status IS 'Seat status: AVAILABLE (can be selected), PENDING (temporarily selected by user), BOOKED (confirmed booking), UNAVAILABLE (not for sale).';

-- Step 6: Verify the constraint
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.constraint_column_usage 
        WHERE constraint_name = 'schedule_seat_status_check'
    ) THEN
        RAISE NOTICE 'SUCCESS: schedule_seat_status_check constraint updated to include PENDING status';
    ELSE
        RAISE WARNING 'WARNING: schedule_seat_status_check constraint not found';
    END IF;
    
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'ScheduleSeat' 
        AND column_name = 'pendingUserId'
    ) THEN
        RAISE NOTICE 'SUCCESS: pendingUserId column exists';
    ELSE
        RAISE WARNING 'WARNING: pendingUserId column not found';
    END IF;
    
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'ScheduleSeat' 
        AND column_name = 'pendingAt'
    ) THEN
        RAISE NOTICE 'SUCCESS: pendingAt column exists';
    ELSE
        RAISE WARNING 'WARNING: pendingAt column not found';
    END IF;
END $$;
