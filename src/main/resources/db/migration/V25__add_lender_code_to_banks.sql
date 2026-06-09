-- V25: Add lender_code to banks table
-- RATIONALE: loan_products.lender_id is BIGINT, banks.id is UUID.
-- The frontend previously used a fragile name-hash bridge to map between them.
-- This migration adds a stable, auto-incrementing numeric identifier to banks
-- so loan_products.lender_id can reliably reference a bank via banks.lender_code.

-- 1. Create a dedicated sequence starting at 200 to avoid collisions with
--    the existing seed values (HDFC=1, L&T=101..PNB=106).
CREATE SEQUENCE IF NOT EXISTS banks_lender_code_seq START WITH 200;

-- 2. Add the column with the sequence as default for new inserts
ALTER TABLE banks ADD COLUMN IF NOT EXISTS lender_code BIGINT UNIQUE;

-- 3. Backfill existing banks with their known lender_id values from seed data
UPDATE banks SET lender_code = 1   WHERE bank_name = 'HDFC Bank'     AND lender_code IS NULL;
UPDATE banks SET lender_code = 101 WHERE bank_name = 'L&T Finance'   AND lender_code IS NULL;
UPDATE banks SET lender_code = 102 WHERE bank_name = 'SBI'           AND lender_code IS NULL;
UPDATE banks SET lender_code = 103 WHERE bank_name = 'Bajaj Finserv' AND lender_code IS NULL;
UPDATE banks SET lender_code = 104 WHERE bank_name = 'Tata Capital'  AND lender_code IS NULL;
UPDATE banks SET lender_code = 105 WHERE bank_name = 'ICICI Bank'    AND lender_code IS NULL;
UPDATE banks SET lender_code = 106 WHERE bank_name = 'PNB Housing'   AND lender_code IS NULL;

-- 4. Assign sequence-generated codes to any remaining banks without a code
UPDATE banks SET lender_code = nextval('banks_lender_code_seq') WHERE lender_code IS NULL;

-- 5. Now make it NOT NULL with a default for future inserts
ALTER TABLE banks ALTER COLUMN lender_code SET NOT NULL;
ALTER TABLE banks ALTER COLUMN lender_code SET DEFAULT nextval('banks_lender_code_seq');
