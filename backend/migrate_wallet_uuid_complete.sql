-- Complete migration script to convert UserWallet and WalletTransaction to use UUID
-- WARNING: This will delete all existing wallet data!
-- Run this script manually in your PostgreSQL database

-- Step 1: Drop dependent tables first (due to foreign key constraints)
DROP TABLE IF EXISTS user_service."WalletTransaction" CASCADE;
DROP TABLE IF EXISTS user_service."UserWallet" CASCADE;

-- Step 2: Recreate UserWallet table with UUID primary key
CREATE TABLE user_service."UserWallet" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "userId" BIGINT NOT NULL UNIQUE,
    balance DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    "lastTransaction" TIMESTAMP,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY ("userId") REFERENCES user_service."User"(id)
);

-- Step 3: Create index on userId
CREATE INDEX idx_wallet_user ON user_service."UserWallet"("userId");

-- Step 4: Recreate WalletTransaction table with UUID foreign key
CREATE TABLE user_service."WalletTransaction" (
    id BIGSERIAL PRIMARY KEY,
    "walletId" UUID NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    "referenceId" VARCHAR(255),
    description TEXT,
    "balanceBefore" DOUBLE PRECISION NOT NULL,
    "balanceAfter" DOUBLE PRECISION NOT NULL,
    metadata TEXT,
    "createdAt" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "completedAt" TIMESTAMP,
    CONSTRAINT fk_transaction_wallet FOREIGN KEY ("walletId") REFERENCES user_service."UserWallet"(id) ON DELETE CASCADE
);

-- Step 5: Create indexes for WalletTransaction
CREATE INDEX idx_transaction_wallet ON user_service."WalletTransaction"("walletId");
CREATE INDEX idx_transaction_reference ON user_service."WalletTransaction"("referenceId");
CREATE INDEX idx_transaction_status ON user_service."WalletTransaction"(status);

-- Verification queries
SELECT 'UserWallet table structure:' as info;
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'user_service' 
  AND table_name = 'UserWallet'
ORDER BY ordinal_position;

SELECT 'WalletTransaction table structure:' as info;
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'user_service' 
  AND table_name = 'WalletTransaction'
ORDER BY ordinal_position;
