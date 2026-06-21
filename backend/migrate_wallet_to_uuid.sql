-- Migration script to convert UserWallet ID from BIGINT to UUID
-- WARNING: This will modify existing data. Backup your database before running!
-- IMPORTANT: Stop all services before running this migration!

BEGIN;

-- Step 1: Create a temporary column to store UUID values in UserWallet
ALTER TABLE "UserWallet" ADD COLUMN IF NOT EXISTS id_uuid UUID;

-- Step 2: Generate UUIDs for existing records (if any)
UPDATE "UserWallet" SET id_uuid = gen_random_uuid() WHERE id_uuid IS NULL;

-- Step 3: Update WalletTransaction to add UUID foreign key column
ALTER TABLE "WalletTransaction" ADD COLUMN IF NOT EXISTS wallet_id_uuid UUID;

-- Step 4: Map the old wallet IDs to new UUIDs in WalletTransaction
UPDATE "WalletTransaction" wt
SET wallet_id_uuid = uw.id_uuid
FROM "UserWallet" uw
WHERE wt."walletId" = uw.id;

-- Step 5: Drop old foreign key constraint on WalletTransaction (if exists)
DO $$ 
BEGIN
    ALTER TABLE "WalletTransaction" DROP CONSTRAINT IF EXISTS fk_wallet_transaction_wallet;
    ALTER TABLE "WalletTransaction" DROP CONSTRAINT IF EXISTS "WalletTransaction_walletId_fkey";
EXCEPTION
    WHEN undefined_object THEN NULL;
END $$;

-- Step 6: Drop the old walletId column in WalletTransaction
ALTER TABLE "WalletTransaction" DROP COLUMN IF EXISTS "walletId";

-- Step 7: Rename the new UUID column to walletId
ALTER TABLE "WalletTransaction" RENAME COLUMN wallet_id_uuid TO "walletId";

-- Step 8: Drop the old id column in UserWallet
DO $$ 
BEGIN
    ALTER TABLE "UserWallet" DROP CONSTRAINT IF EXISTS "UserWallet_pkey";
EXCEPTION
    WHEN undefined_object THEN NULL;
END $$;

ALTER TABLE "UserWallet" DROP COLUMN IF EXISTS id;

-- Step 9: Rename the UUID column to id and set it as primary key
ALTER TABLE "UserWallet" RENAME COLUMN id_uuid TO id;
ALTER TABLE "UserWallet" ADD PRIMARY KEY (id);

-- Step 10: Add NOT NULL constraint to walletId in WalletTransaction
ALTER TABLE "WalletTransaction" ALTER COLUMN "walletId" SET NOT NULL;

-- Step 11: Add foreign key constraint back to WalletTransaction
ALTER TABLE "WalletTransaction" 
ADD CONSTRAINT fk_wallet_transaction_wallet 
FOREIGN KEY ("walletId") REFERENCES "UserWallet"(id) ON DELETE CASCADE;

-- Step 12: Create index on walletId in WalletTransaction for performance
CREATE INDEX IF NOT EXISTS idx_wallet_transaction_wallet ON "WalletTransaction"("walletId");

-- Step 13: Drop and recreate the sequence if it exists (not needed for UUID)
DROP SEQUENCE IF EXISTS "UserWallet_id_seq" CASCADE;

COMMIT;

-- Verification queries (uncomment and run these to check the migration)
-- SELECT id, "userId", balance FROM "UserWallet" LIMIT 5;
-- SELECT id, "walletId", amount FROM "WalletTransaction" LIMIT 5;
-- \d "UserWallet"
-- \d "WalletTransaction"
