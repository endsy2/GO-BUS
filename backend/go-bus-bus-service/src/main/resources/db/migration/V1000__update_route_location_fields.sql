-- Migration: Split location field into originLocation and destinationLocation
-- Date: 2026-05-18
-- Description: Replace single location field with separate origin and destination location fields

-- Add new columns
ALTER TABLE "BusRoute" 
ADD COLUMN IF NOT EXISTS "originLocation" jsonb,
ADD COLUMN IF NOT EXISTS "destinationLocation" jsonb;

-- Migrate existing data (if location field exists and has data)
-- This assumes the old location field represented the origin location
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'BusRoute' 
        AND column_name = 'location'
    ) THEN
        -- Copy existing location data to originLocation
        UPDATE "BusRoute" 
        SET "originLocation" = location 
        WHERE location IS NOT NULL;
        
        -- Drop the old location column
        ALTER TABLE "BusRoute" DROP COLUMN IF EXISTS location;
    END IF;
END $$;

-- Add comments for documentation
COMMENT ON COLUMN "BusRoute"."originLocation" IS 'GPS coordinates for origin point in JSON format: {"lat": 11.5564, "lng": 104.9282}';
COMMENT ON COLUMN "BusRoute"."destinationLocation" IS 'GPS coordinates for destination point in JSON format: {"lat": 13.3671, "lng": 103.8448}';
