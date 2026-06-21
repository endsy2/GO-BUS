-- Check current UserWallet table structure
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'UserWallet'
ORDER BY ordinal_position;

-- Check current WalletTransaction table structure
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'WalletTransaction'
ORDER BY ordinal_position;

-- Check if there are any existing wallet records
SELECT COUNT(*) as wallet_count FROM "UserWallet";
SELECT COUNT(*) as transaction_count FROM "WalletTransaction";
