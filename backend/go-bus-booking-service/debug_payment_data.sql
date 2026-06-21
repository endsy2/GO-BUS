-- Debug script to check Payment table data
-- Run this in your PostgreSQL database to understand why getRevenueByPaymentMethod returns empty

-- 1. Check total payment count
SELECT COUNT(*) as total_payments FROM "Payment";

-- 2. Check payment status distribution
SELECT status, COUNT(*) as count 
FROM "Payment" 
GROUP BY status 
ORDER BY count DESC;

-- 3. Check payments with SUCCESS status
SELECT COUNT(*) as success_payments 
FROM "Payment" 
WHERE status = 'SUCCESS';

-- 4. Check if paidAt is populated for SUCCESS payments
SELECT 
    COUNT(*) as total_success,
    COUNT("paidAt") as with_paid_at,
    COUNT(*) - COUNT("paidAt") as missing_paid_at
FROM "Payment" 
WHERE status = 'SUCCESS';

-- 5. Check payment methods for SUCCESS payments
SELECT 
    method, 
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM "Payment" 
WHERE status = 'SUCCESS' 
  AND "paidAt" IS NOT NULL
GROUP BY method
ORDER BY total_amount DESC;

-- 6. Check date range of SUCCESS payments
SELECT 
    MIN("paidAt") as earliest_payment,
    MAX("paidAt") as latest_payment,
    COUNT(*) as total_count
FROM "Payment" 
WHERE status = 'SUCCESS' 
  AND "paidAt" IS NOT NULL;

-- 7. Sample of recent SUCCESS payments
SELECT 
    id,
    method,
    amount,
    status,
    "paidAt",
    "transactionId"
FROM "Payment" 
WHERE status = 'SUCCESS'
ORDER BY "paidAt" DESC 
LIMIT 10;

-- 8. Check for payments in the last 7 days
SELECT 
    method,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM "Payment" 
WHERE status = 'SUCCESS' 
  AND "paidAt" IS NOT NULL
  AND "paidAt" >= NOW() - INTERVAL '7 days'
GROUP BY method;

-- 9. Check all payments regardless of status (last 7 days)
SELECT 
    status,
    method,
    COUNT(*) as count,
    SUM(amount) as total_amount
FROM "Payment" 
WHERE "paidAt" >= NOW() - INTERVAL '7 days'
GROUP BY status, method
ORDER BY status, count DESC;
