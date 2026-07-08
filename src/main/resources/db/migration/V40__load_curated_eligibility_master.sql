-- V40__load_curated_eligibility_master.sql
-- Replaces existing eligibility rules with curated final Excel

-- 0. Expand column size for longer product codes and employment types
ALTER TABLE loan_products ALTER COLUMN product_code TYPE VARCHAR(100);
ALTER TABLE eligibility_conditions ALTER COLUMN product_code TYPE VARCHAR(100);
ALTER TABLE eligibility_conditions ALTER COLUMN employment_type TYPE VARCHAR(100);

-- 1. Upsert Loan Products
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('LT-Secured-0001', 'L&T Finance LAP Program', 'LAP', 101, 'L&T Finance', 'Floating',
     650, 900, 10.0, 36, 360, 2000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('LT-Secured-0002', 'L&T Finance LAP Program', 'LAP', 101, 'L&T Finance', 'Floating',
     650, 900, 10.0, 36, 240, 2000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('LT-Secured-0003', 'L&T Finance LAP Program', 'LAP', 101, 'L&T Finance', 'Floating',
     650, 900, 10.0, 36, 240, 2000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('LT-Secured-0004', 'L&T Finance LAP Program', 'LAP', 101, 'L&T Finance', 'Floating',
     650, 900, 10.0, 36, 240, 2000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('LT-Secured-0005', 'L&T Finance LAP Program', 'LAP', 101, 'L&T Finance', 'Floating',
     650, 900, 10.0, 36, 240, 2000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0001', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 240, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0002', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 240, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0003', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 240, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0004', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 240, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0001', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 300, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0002', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 300, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0003', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 300, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0004', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 300, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0001', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 300, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0002', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 240, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0005', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 240, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0006', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 240, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BOB-Secured-0001', 'Bank of Baroda LAP Program', 'LAP', 202, 'Bank of Baroda', 'Floating',
     650, 900, 10.0, 36, 360, 500000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BOB-Secured-0002', 'Bank of Baroda LAP Program', 'LAP', 202, 'Bank of Baroda', 'Floating',
     650, 900, 10.0, 36, 360, 500000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('STATEBANKOFINDIA-Secured-0001', 'SBI LAP Program', 'LAP', 102, 'SBI', 'Floating',
     550, 900, 10.0, 24, 360, 1000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('STATEBANKOFINDIA-Secured-0002', 'SBI LAP Program', 'LAP', 102, 'SBI', 'Floating',
     550, 900, 10.0, 24, 360, 1000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0001', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 384, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0002', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 384, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0003', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 384, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0006', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 384, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0007', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 384, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('YESBANK-Secured-0001', 'YES BANK LAP Program', 'LAP', 205, 'YES BANK', 'Floating',
     680, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('YESBANK-Secured-0002', 'YES BANK LAP Program', 'LAP', 205, 'YES BANK', 'Floating',
     680, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('YESBANK-Secured-0004', 'YES BANK LAP Program', 'LAP', 205, 'YES BANK', 'Floating',
     680, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('YESBANK-Secured-0005', 'YES BANK LAP Program', 'LAP', 205, 'YES BANK', 'Floating',
     680, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('YESBANK-Secured-0006', 'YES BANK LAP Program', 'LAP', 205, 'YES BANK', 'Floating',
     680, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('YESBANK-Secured-0007', 'YES BANK LAP Program', 'LAP', 205, 'YES BANK', 'Floating',
     680, 900, 10.0, 60, 180, 2100000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0001', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 360, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0002', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0003', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 240, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0004', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 300, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0005', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 180, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0006', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 240, 2100000, 150000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0007', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     750, 900, 10.0, 60, 240, 2100000, 20000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0001', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 300, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0002', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 240, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0005', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 240, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0006', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 240, 3000000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0007', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 240, 3000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDBIBANK-Secured-0001', 'IDBI LAP Program', 'LAP', 207, 'IDBI', 'Floating',
     700, 900, 10.0, 60, 360, 5000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDBIBANK-Secured-0002', 'IDBI LAP Program', 'LAP', 207, 'IDBI', 'Floating',
     700, 900, 10.0, 60, 300, 5000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('TATACAPITAL-Secured-0001', 'Tata Capital LAP Program', 'LAP', 208, 'Tata Capital', 'Floating',
     650, 900, 10.0, 60, 360, 1000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('TATACAPITAL-Secured-0002', 'Tata Capital LAP Program', 'LAP', 208, 'Tata Capital', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('TATACAPITAL-Secured-0003', 'Tata Capital LAP Program', 'LAP', 208, 'Tata Capital', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0001', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 360, 1000000, 200000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0002', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 360, 1000000, 200000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0003', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 360, 1000000, 200000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0004', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 360, 1000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0005', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 360, 1000000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('LT-Secured-0006', 'L&T Finance LAP Program', 'LAP', 101, 'L&T Finance', 'Floating',
     650, 900, 10.0, 36, 240, 2000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0005', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 180, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0006', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 180, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0007', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 180, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ICICI-Secured-0008', 'ICICI Bank LAP Program', 'LAP', 105, 'ICICI Bank', 'Floating',
     700, 900, 10.0, 60, 180, 2000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0005', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 180, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0006', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 180, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0007', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 180, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BANDHAN-Secured-0008', 'Bandhan Bank LAP Program', 'LAP', 200, 'Bandhan Bank', 'Floating',
     700, 900, 10.0, 60, 180, 200000, 80000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0007', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 180, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0008', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 180, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0011', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 180, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('ABFL-Secured-0012', 'Aditya Birla Finance Limited LAP Program', 'LAP', 201, 'Aditya Birla Finance Limited', 'Floating',
     675, 900, 10.0, 36, 180, 3500000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BOB-Secured-0003', 'Bank of Baroda LAP Program', 'LAP', 202, 'Bank of Baroda', 'Floating',
     650, 900, 10.0, 36, 144, 500000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BOB-Secured-0004', 'Bank of Baroda LAP Program', 'LAP', 202, 'Bank of Baroda', 'Floating',
     650, 900, 10.0, 36, 144, 500000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('STATEBANKOFINDIA-Secured-0003', 'SBI LAP Program', 'LAP', 102, 'SBI', 'Floating',
     600, 900, 10.0, 60, 180, 1000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('STATEBANKOFINDIA-Secured-0004', 'SBI LAP Program', 'LAP', 102, 'SBI', 'Floating',
     600, 900, 10.0, 60, 180, 1000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0008', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 240, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0009', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 240, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0010', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 240, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0013', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 240, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('BAJAJ-Secured-0014', 'Bajaj Finance LAP Program', 'LAP', 203, 'Bajaj Finance', 'Floating',
     680, 900, 10.0, 120, 240, 3500000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0008', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 180, 1100000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0009', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 180, 1100000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0010', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 180, 1100000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('HDFC-Secured-0011', 'HDFC Bank LAP Program', 'LAP', 1, 'HDFC Bank', 'Floating',
     650, 900, 10.0, 60, 180, 1100000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDFCBANK-Secured-0001', 'IDFC LAP Program', 'LAP', 209, 'IDFC', 'Floating',
     600, 900, 10.0, 60, 300, 5000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDFCBANK-Secured-0002', 'IDFC LAP Program', 'LAP', 209, 'IDFC', 'Floating',
     600, 900, 10.0, 60, 300, 5000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDFCBANK-Secured-0005', 'IDFC LAP Program', 'LAP', 209, 'IDFC', 'Floating',
     600, 900, 10.0, 60, 300, 5000000, 30000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0008', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 180, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0009', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 180, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0012', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 180, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0013', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 144, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0014', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 144, 3000000, 500000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0015', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 180, 3000000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('JIO-Secured-0016', 'JIO Finance LAP Program', 'LAP', 206, 'JIO Finance', 'Floating',
     650, 900, 10.0, 60, 180, 3000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDBIBANK-Secured-0003', 'IDBI LAP Program', 'LAP', 207, 'IDBI', 'Floating',
     700, 900, 10.0, 60, 240, 5000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('IDBIBANK-Secured-0004', 'IDBI LAP Program', 'LAP', 207, 'IDBI', 'Floating',
     700, 900, 10.0, 60, 180, 5000000, 99999999, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('TATACAPITAL-Secured-0006', 'Tata Capital LAP Program', 'LAP', 208, 'Tata Capital', 'Floating',
     650, 900, 10.0, 60, 180, 1000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('TATACAPITAL-Secured-0007', 'Tata Capital LAP Program', 'LAP', 208, 'Tata Capital', 'Floating',
     650, 900, 10.0, 60, 180, 1000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('TATACAPITAL-Secured-0008', 'Tata Capital LAP Program', 'LAP', 208, 'Tata Capital', 'Floating',
     650, 900, 10.0, 60, 180, 1000000, 100000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0008', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 200000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0009', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 200000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0010', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 200000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0011', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 50000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('INDUSINDBANK-Secured-0012', 'Indus Ind Bank LAP Program', 'LAP', 210, 'Indus Ind Bank', 'Floating',
     650, 900, 10.0, 60, 240, 1000000, 75000000, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;

-- 2. Clear old eligibility conditions
DELETE FROM eligibility_conditions;

-- 3. Insert curated eligibility conditions
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 30000, 23, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0003'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0003'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 80);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 80);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 80);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BOB-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'BOB-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 10000, 21, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BOB-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'BOB-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 10000, 21, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('STATEBANKOFINDIA-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'STATEBANKOFINDIA-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     550, 25000, 20, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('STATEBANKOFINDIA-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'STATEBANKOFINDIA-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     550, 25000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0004'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0004'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0005'), 'Self Employed Professional', 'CPM SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0005'), 'Self Employed Professional', 'CPM SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 21, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0002'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 21, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0003'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 21, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     750, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0005'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDBIBANK-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'IDBIBANK-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     700, 40000, 22, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDBIBANK-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'IDBIBANK-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     700, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0006'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 30000, 23, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0003'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('LT-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'LT-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 25000, 25, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0005'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ICICI-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'ICICI-Secured-0008'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     700, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0005'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BANDHAN-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'BANDHAN-Secured-0008'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     700, 15000, 21, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0007'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0008'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 80);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0011', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0011'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 80);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('ABFL-Secured-0012', (SELECT id FROM loan_products WHERE product_code = 'ABFL-Secured-0012'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     675, 30000, 22, 80);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BOB-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'BOB-Secured-0003'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 10000, 21, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BOB-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'BOB-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 10000, 21, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('STATEBANKOFINDIA-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'STATEBANKOFINDIA-Secured-0003'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     600, 25000, 20, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('STATEBANKOFINDIA-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'STATEBANKOFINDIA-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     600, 25000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0008'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0009', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0009'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0010', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0010'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0013', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0013'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0014', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0014'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('BAJAJ-Secured-0014', (SELECT id FROM loan_products WHERE product_code = 'BAJAJ-Secured-0014'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     680, 30000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0004'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0004'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0005'), 'Self Employed Professional', 'CPM SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0005'), 'Self Employed Professional', 'CPM SEP', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0006'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('YESBANK-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'YESBANK-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     680, 40000, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0008'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 30000, 20, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0009', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0009'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0010', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0010'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0011', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0011'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0011', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0011'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('HDFC-Secured-0011', (SELECT id FROM loan_products WHERE product_code = 'HDFC-Secured-0011'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 30000, 20, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDFCBANK-Secured-0001', (SELECT id FROM loan_products WHERE product_code = 'IDFCBANK-Secured-0001'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     600, 0, 22, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDFCBANK-Secured-0002', (SELECT id FROM loan_products WHERE product_code = 'IDFCBANK-Secured-0002'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     600, 0, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDFCBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'IDFCBANK-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     600, 0, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDFCBANK-Secured-0005', (SELECT id FROM loan_products WHERE product_code = 'IDFCBANK-Secured-0005'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     600, 0, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0008'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 0, 23, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0009', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0009'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0012', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0012'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0012', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0012'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0013', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0013'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 0, 23, 60);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0014', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0014'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0015', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0015'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0016', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0016'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0016', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0016'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('JIO-Secured-0016', (SELECT id FROM loan_products WHERE product_code = 'JIO-Secured-0016'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 0, 23, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDBIBANK-Secured-0003', (SELECT id FROM loan_products WHERE product_code = 'IDBIBANK-Secured-0003'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     700, 40000, 22, 65);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('IDBIBANK-Secured-0004', (SELECT id FROM loan_products WHERE product_code = 'IDBIBANK-Secured-0004'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     700, 40000, 22, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0006', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0006'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0007', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0007'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0008'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('TATACAPITAL-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'TATACAPITAL-Secured-0008'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 23, 70);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0008', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0008'), 'Salaried', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 62);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0009', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0009'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0010', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0010'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0010', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0010'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0010', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0010'), 'Self Employed Professional', 'SEP', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0011', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0011'), 'Self Employed Professional/Self Employed Non Professional', 'GST', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('INDUSINDBANK-Secured-0012', (SELECT id FROM loan_products WHERE product_code = 'INDUSINDBANK-Secured-0012'), 'Self Employed Professional/Self Employed Non Professional', 'BANKING', 'Any', 'false', 'false', 'false',
     650, 40000, 24, 75);
