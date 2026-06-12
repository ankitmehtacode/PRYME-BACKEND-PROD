-- ═══════════════════════════════════════════════════════════════════════════════
-- V27 — MASTER ROI MATRIX INGESTION
-- ═══════════════════════════════════════════════════════════════════════════════
-- Wipes all existing ROI matrix data and replaces with new master chart.
-- 9 lenders · 38 product codes · ~434 pricing tiers.
-- Uses PL/pgSQL for safe per-product lookups (skips if product_code missing).
-- ═══════════════════════════════════════════════════════════════════════════════

-- Ensure YES BANK exists in the banks table
INSERT INTO banks (bank_name, active, lender_code)
VALUES ('YES BANK', true, 205)
ON CONFLICT (bank_name) DO NOTHING;

-- Step 1: Wipe all existing ROI, conditions, and product records
DELETE FROM product_roi_matrix;
DELETE FROM eligibility_conditions;
DELETE FROM loan_products;

-- Step 1.5: Seed new loan products with the new nomenclature
INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, min_loan_amount, max_loan_amount, min_tenure_months, max_tenure_months, is_active, processing_fee, login_fees)
VALUES
('ABFL-HL-0001', 'ABFL Home Loan Salaried/SEP', 201, 'Aditya Birla Finance Limited', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 2950),
('ABFL-HL-0002', 'ABFL Home Loan SENP', 201, 'Aditya Birla Finance Limited', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 2950),
('ABFL-LAP-0001', 'ABFL LAP Salaried/SEP', 201, 'Aditya Birla Finance Limited', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5900),
('ABFL-LAP-0002', 'ABFL LAP SENP', 201, 'Aditya Birla Finance Limited', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5900),

('BAJAJ-HL-0001', 'Bajaj Home Loan Salaried', 203, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 2000),
('BAJAJ-HL-0002', 'Bajaj Home Loan SENP', 203, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3500),
('BAJAJ-HL-0003', 'Bajaj Home Loan SENP Industry Margin', 203, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3500),
('BAJAJ-LAP-0001', 'Bajaj LAP Salaried', 203, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 2000),
('BAJAJ-LAP-0002', 'Bajaj LAP SENP', 203, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 3540),
('BAJAJ-LAP-0003', 'Bajaj LAP SENP Industry Margin', 203, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 3540),

('BANDHAN-HL-0001', 'Bandhan Home Loan Salaried/SEP', 200, 'Bandhan Bank', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 2360),
('BANDHAN-HL-0002', 'Bandhan Home Loan SENP', 200, 'Bandhan Bank', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 2360),
('BANDHAN-LAP-0001', 'Bandhan LAP Salaried/SEP', 200, 'Bandhan Bank', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 2360),
('BANDHAN-LAP-0002', 'Bandhan LAP SENP', 200, 'Bandhan Bank', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 2360),

('BOB-HL-0001', 'BOB Home Loan Salaried/SEP', 202, 'Bank of Baroda', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 0),
('BOB-HL-0002', 'BOB Home Loan SENP', 202, 'Bank of Baroda', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 0),
('BOB-LAP-0001', 'BOB LAP Salaried/SEP', 202, 'Bank of Baroda', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 0),
('BOB-LAP-0002', 'BOB LAP SENP', 202, 'Bank of Baroda', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 0),

('HDFC-HL-0001', 'HDFC Home Loan Salaried/SEP', 1, 'HDFC Bank', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3000),
('HDFC-HL-0002', 'HDFC Home Loan SENP', 1, 'HDFC Bank', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3000),
('HDFC-LAP-0001', 'HDFC LAP Salaried/SEP', 1, 'HDFC Bank', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5000),
('HDFC-LAP-0002', 'HDFC LAP SENP', 1, 'HDFC Bank', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5000),

('ICICI-HL-0001', 'ICICI Home Loan Salaried/SEP', 105, 'ICICI Bank', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3000),
('ICICI-HL-0002', 'ICICI Home Loan SENP', 105, 'ICICI Bank', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3000),
('ICICI-LAP-0001', 'ICICI LAP Salaried/SEP', 105, 'ICICI Bank', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5000),
('ICICI-LAP-0002', 'ICICI LAP SENP', 105, 'ICICI Bank', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5000),

('LT-HL-0001', 'L&T Home Loan Salaried', 101, 'L&T Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 1000),
('LT-HL-0002', 'L&T Home Loan SEP/SENP', 101, 'L&T Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 1000),
('LT-LAP-0001', 'L&T LAP Salaried', 101, 'L&T Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 1000),
('LT-LAP-0002', 'L&T LAP SEP/SENP', 101, 'L&T Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 1000),

('SBI-HL-0001', 'SBI Home Loan Salaried/SEP', 102, 'SBI', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 0),
('SBI-HL-0002', 'SBI Home Loan SENP', 102, 'SBI', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 0),
('SBI-LAP-0001', 'SBI LAP Salaried/SEP', 102, 'SBI', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 0),
('SBI-LAP-0002', 'SBI LAP SENP', 102, 'SBI', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 0),

('YES-HL-0001', 'YES BANK Home Loan Salaried/SEP', 205, 'YES BANK', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3000),
('YES-HL-0002', 'YES BANK Home Loan SENP', 205, 'YES BANK', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 3000),
('YES-LAP-0001', 'YES BANK LAP Salaried/SEP', 205, 'YES BANK', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5000),
('YES-LAP-0002', 'YES BANK LAP SENP', 205, 'YES BANK', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0150, 5000);

-- Step 1.6: Seed default eligibility conditions matching the new products
INSERT INTO eligibility_conditions (product_id, product_code, employment_type, min_age, max_age, min_income, work_exp_years, business_age_years, cibil_min, property_type, bank_name, loan_type, is_active)
SELECT 
    id, 
    product_code,
    CASE 
        WHEN product_code LIKE '%-HL-0001' OR product_code LIKE '%-LAP-0001' THEN 'Salaried'
        ELSE 'SEP/SENP'
    END,
    21, 65, 25000,
    CASE 
        WHEN product_code LIKE '%-HL-0001' OR product_code LIKE '%-LAP-0001' THEN 1
        ELSE NULL
    END,
    CASE 
        WHEN product_code LIKE '%-HL-0001' OR product_code LIKE '%-LAP-0001' THEN NULL
        ELSE 3
    END,
    650,
    'RESIDENTIAL, COMMERCIAL, PLOT',
    lender_name,
    loan_type,
    true
FROM loan_products;

-- Step 2: Insert new master ROI matrix
DO $$
DECLARE
    pid BIGINT;
BEGIN

-- ═══════════════════════════════════════════════════════════════════════════════
-- ABFL — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'ABFL-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.081),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0815),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.082),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.087),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.089),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.096),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.087);
END IF;

-- ABFL — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'ABFL-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0815),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.082),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0825),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0875),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0895),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0965),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0875);
END IF;

-- ABFL — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'ABFL-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.1),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.1005),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.101),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.106),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.108),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.115),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.106);
END IF;

-- ABFL — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'ABFL-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.1005),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.101),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.1015),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.1065),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.1085),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.1155),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.1065);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- BAJAJ — Home Loan Salaried (3 loan-amount slabs)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'Salaried', 0, 3000000, 800, 900, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 780, 799, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 750, 779, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 730, 749, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 700, 729, false, 0.0975),
    (pid, 'Salaried', 0, 3000000, 650, 700, false, 0.105),
    (pid, 'Salaried', 0, 3000000, NULL, NULL, true, 0.0975),
    (pid, 'Salaried', 3000000, 7500000, 800, 900, false, 0.0925),
    (pid, 'Salaried', 3000000, 7500000, 780, 799, false, 0.095),
    (pid, 'Salaried', 3000000, 7500000, 750, 779, false, 0.095),
    (pid, 'Salaried', 3000000, 7500000, 730, 749, false, 0.095),
    (pid, 'Salaried', 3000000, 7500000, 700, 729, false, 0.0975),
    (pid, 'Salaried', 3000000, 7500000, 650, 700, false, 0.105),
    (pid, 'Salaried', 3000000, 7500000, NULL, NULL, true, 0.0975),
    (pid, 'Salaried', 7500000, 999999999, 800, 900, false, 0.0925),
    (pid, 'Salaried', 7500000, 999999999, 780, 799, false, 0.095),
    (pid, 'Salaried', 7500000, 999999999, 750, 779, false, 0.095),
    (pid, 'Salaried', 7500000, 999999999, 730, 749, false, 0.095),
    (pid, 'Salaried', 7500000, 999999999, 700, 729, false, 0.0975),
    (pid, 'Salaried', 7500000, 999999999, 650, 700, false, 0.105),
    (pid, 'Salaried', 7500000, 999999999, NULL, NULL, true, 0.0975);
END IF;

-- BAJAJ — Home Loan SENP (3 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 3000000, 800, 900, false, 0.097),
    (pid, 'SENP', 0, 3000000, 780, 799, false, 0.0995),
    (pid, 'SENP', 0, 3000000, 750, 779, false, 0.0995),
    (pid, 'SENP', 0, 3000000, 730, 749, false, 0.0995),
    (pid, 'SENP', 0, 3000000, 700, 729, false, 0.1015),
    (pid, 'SENP', 0, 3000000, 650, 700, false, 0.1085),
    (pid, 'SENP', 0, 3000000, NULL, NULL, true, 0.1015),
    (pid, 'SENP', 3000000, 7500000, 800, 900, false, 0.0995),
    (pid, 'SENP', 3000000, 7500000, 780, 799, false, 0.102),
    (pid, 'SENP', 3000000, 7500000, 750, 779, false, 0.102),
    (pid, 'SENP', 3000000, 7500000, 730, 749, false, 0.102),
    (pid, 'SENP', 3000000, 7500000, 700, 729, false, 0.104),
    (pid, 'SENP', 3000000, 7500000, 650, 700, false, 0.111),
    (pid, 'SENP', 3000000, 7500000, NULL, NULL, true, 0.104),
    (pid, 'SENP', 7500000, 999999999, 800, 900, false, 0.102),
    (pid, 'SENP', 7500000, 999999999, 780, 799, false, 0.1045),
    (pid, 'SENP', 7500000, 999999999, 750, 779, false, 0.1045),
    (pid, 'SENP', 7500000, 999999999, 730, 749, false, 0.1045),
    (pid, 'SENP', 7500000, 999999999, 700, 729, false, 0.1065),
    (pid, 'SENP', 7500000, 999999999, 650, 700, false, 0.1135),
    (pid, 'SENP', 7500000, 999999999, NULL, NULL, true, 0.1065);
END IF;

-- BAJAJ — Home Loan SENP Industry Margin (3 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-HL-0003';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP (Industry Margin)', 0, 3000000, 800, 900, false, 0.1045),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 780, 799, false, 0.107),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 750, 779, false, 0.107),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 730, 749, false, 0.107),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 700, 729, false, 0.109),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 650, 700, false, 0.116),
    (pid, 'SENP (Industry Margin)', 0, 3000000, NULL, NULL, true, 0.109),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 800, 900, false, 0.107),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 780, 799, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 750, 779, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 730, 749, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 700, 729, false, 0.1115),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 650, 700, false, 0.1185),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, NULL, NULL, true, 0.1115),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 800, 900, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 780, 799, false, 0.112),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 750, 779, false, 0.112),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 730, 749, false, 0.112),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 700, 729, false, 0.114),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 650, 700, false, 0.121),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, NULL, NULL, true, 0.114);
END IF;

-- BAJAJ — LAP Salaried (3 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'Salaried', 0, 3000000, 800, 900, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 780, 799, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 750, 779, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 730, 749, false, 0.0925),
    (pid, 'Salaried', 0, 3000000, 700, 729, false, 0.0975),
    (pid, 'Salaried', 0, 3000000, 650, 700, false, 0.105),
    (pid, 'Salaried', 0, 3000000, NULL, NULL, true, 0.0975),
    (pid, 'Salaried', 3000000, 7500000, 800, 900, false, 0.0925),
    (pid, 'Salaried', 3000000, 7500000, 780, 799, false, 0.095),
    (pid, 'Salaried', 3000000, 7500000, 750, 779, false, 0.095),
    (pid, 'Salaried', 3000000, 7500000, 730, 749, false, 0.095),
    (pid, 'Salaried', 3000000, 7500000, 700, 729, false, 0.0975),
    (pid, 'Salaried', 3000000, 7500000, 650, 700, false, 0.105),
    (pid, 'Salaried', 3000000, 7500000, NULL, NULL, true, 0.0975),
    (pid, 'Salaried', 7500000, 999999999, 800, 900, false, 0.0925),
    (pid, 'Salaried', 7500000, 999999999, 780, 799, false, 0.095),
    (pid, 'Salaried', 7500000, 999999999, 750, 779, false, 0.095),
    (pid, 'Salaried', 7500000, 999999999, 730, 749, false, 0.095),
    (pid, 'Salaried', 7500000, 999999999, 700, 729, false, 0.0975),
    (pid, 'Salaried', 7500000, 999999999, 650, 700, false, 0.105),
    (pid, 'Salaried', 7500000, 999999999, NULL, NULL, true, 0.0975);
END IF;

-- BAJAJ — LAP SENP (3 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 3000000, 800, 900, false, 0.097),
    (pid, 'SENP', 0, 3000000, 780, 799, false, 0.0995),
    (pid, 'SENP', 0, 3000000, 750, 779, false, 0.0995),
    (pid, 'SENP', 0, 3000000, 730, 749, false, 0.0995),
    (pid, 'SENP', 0, 3000000, 700, 729, false, 0.1015),
    (pid, 'SENP', 0, 3000000, 650, 700, false, 0.1085),
    (pid, 'SENP', 0, 3000000, NULL, NULL, true, 0.1015),
    (pid, 'SENP', 3000000, 7500000, 800, 900, false, 0.0995),
    (pid, 'SENP', 3000000, 7500000, 780, 799, false, 0.102),
    (pid, 'SENP', 3000000, 7500000, 750, 779, false, 0.102),
    (pid, 'SENP', 3000000, 7500000, 730, 749, false, 0.102),
    (pid, 'SENP', 3000000, 7500000, 700, 729, false, 0.104),
    (pid, 'SENP', 3000000, 7500000, 650, 700, false, 0.111),
    (pid, 'SENP', 3000000, 7500000, NULL, NULL, true, 0.104),
    (pid, 'SENP', 7500000, 999999999, 800, 900, false, 0.102),
    (pid, 'SENP', 7500000, 999999999, 780, 799, false, 0.1045),
    (pid, 'SENP', 7500000, 999999999, 750, 779, false, 0.1045),
    (pid, 'SENP', 7500000, 999999999, 730, 749, false, 0.1045),
    (pid, 'SENP', 7500000, 999999999, 700, 729, false, 0.1065),
    (pid, 'SENP', 7500000, 999999999, 650, 700, false, 0.1135),
    (pid, 'SENP', 7500000, 999999999, NULL, NULL, true, 0.1065);
END IF;

-- BAJAJ — LAP SENP Industry Margin (3 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-LAP-0003';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP (Industry Margin)', 0, 3000000, 800, 900, false, 0.1045),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 780, 799, false, 0.107),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 750, 779, false, 0.107),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 730, 749, false, 0.107),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 700, 729, false, 0.109),
    (pid, 'SENP (Industry Margin)', 0, 3000000, 650, 700, false, 0.116),
    (pid, 'SENP (Industry Margin)', 0, 3000000, NULL, NULL, true, 0.109),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 800, 900, false, 0.107),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 780, 799, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 750, 779, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 730, 749, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 700, 729, false, 0.1115),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, 650, 700, false, 0.1185),
    (pid, 'SENP (Industry Margin)', 3000000, 7500000, NULL, NULL, true, 0.1115),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 800, 900, false, 0.1095),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 780, 799, false, 0.112),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 750, 779, false, 0.112),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 730, 749, false, 0.112),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 700, 729, false, 0.114),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, 650, 700, false, 0.121),
    (pid, 'SENP (Industry Margin)', 7500000, 999999999, NULL, NULL, true, 0.114);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- BANDHAN — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'BANDHAN-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.079),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0795),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.08),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.085),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.087),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.094),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.085);
END IF;

-- BANDHAN — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BANDHAN-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0795),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.08),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0805),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0855),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0875),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0945),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0855);
END IF;

-- BANDHAN — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.0975),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.098),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.0985),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.1035),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.1055),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.1125),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.1035);
END IF;

-- BANDHAN — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BANDHAN-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.098),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.0985),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.099),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.104),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.106),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.113),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.104);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- BOB — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'BOB-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.071),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0715),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.072),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.077),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.079),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.086),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.077);
END IF;

-- BOB — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BOB-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0715),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.072),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0725),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0775),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0795),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0865),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0775);
END IF;

-- BOB — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BOB-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.0875),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.088),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.0885),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.0935),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.0955),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.1025),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.0935);
END IF;

-- BOB — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'BOB-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.088),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.0885),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.089),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.094),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.096),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.103),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.094);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- HDFC — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'HDFC-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.0715),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.072),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.0725),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.0785),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.0795),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.0875),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.0785);
END IF;

-- HDFC — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'HDFC-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.072),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.0725),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0735),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0795),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0805),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0885),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0795);
END IF;

-- HDFC — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'HDFC-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.084),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0845),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.085),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.09),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.092),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.099),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.09);
END IF;

-- HDFC — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'HDFC-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0845),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.085),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0855),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0905),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0925),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0995),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0905);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- ICICI — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'ICICI-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.075),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0755),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.076),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.081),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.083),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.09),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.081);
END IF;

-- ICICI — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'ICICI-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0755),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.076),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0765),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0815),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0835),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0905),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0815);
END IF;

-- ICICI — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'ICICI-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.085),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0855),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.086),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.091),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.093),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.1),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.091);
END IF;

-- ICICI — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'ICICI-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0855),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.086),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0865),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0915),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0935),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.1005),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0915);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- L&T — Home Loan Salaried (4 loan-amount slabs)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'LT-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'Salaried', 0, 5000000, 800, 900, false, 0.0795),
    (pid, 'Salaried', 0, 5000000, 780, 799, false, 0.082),
    (pid, 'Salaried', 0, 5000000, 750, 779, false, 0.082),
    (pid, 'Salaried', 0, 5000000, 730, 749, false, 0.0835),
    (pid, 'Salaried', 0, 5000000, 700, 729, false, 0.0835),
    (pid, 'Salaried', 0, 5000000, 650, 700, false, 0.089),
    (pid, 'Salaried', 0, 5000000, NULL, NULL, true, 0.0835),
    (pid, 'Salaried', 5000000, 10000000, 800, 900, false, 0.078),
    (pid, 'Salaried', 5000000, 10000000, 780, 799, false, 0.0815),
    (pid, 'Salaried', 5000000, 10000000, 750, 779, false, 0.0815),
    (pid, 'Salaried', 5000000, 10000000, 730, 749, false, 0.083),
    (pid, 'Salaried', 5000000, 10000000, 700, 729, false, 0.083),
    (pid, 'Salaried', 5000000, 10000000, 650, 700, false, 0.088),
    (pid, 'Salaried', 5000000, 10000000, NULL, NULL, true, 0.083),
    (pid, 'Salaried', 10000000, 15000000, 800, 900, false, 0.078),
    (pid, 'Salaried', 10000000, 15000000, 780, 799, false, 0.08),
    (pid, 'Salaried', 10000000, 15000000, 750, 779, false, 0.08),
    (pid, 'Salaried', 10000000, 15000000, 730, 749, false, 0.0815),
    (pid, 'Salaried', 10000000, 15000000, 700, 729, false, 0.0815),
    (pid, 'Salaried', 10000000, 15000000, 650, 700, false, 0.0865),
    (pid, 'Salaried', 10000000, 15000000, NULL, NULL, true, 0.0815),
    (pid, 'Salaried', 15000000, 999999999, 800, 900, false, 0.0775),
    (pid, 'Salaried', 15000000, 999999999, 780, 799, false, 0.0795),
    (pid, 'Salaried', 15000000, 999999999, 750, 779, false, 0.0795),
    (pid, 'Salaried', 15000000, 999999999, 730, 749, false, 0.081),
    (pid, 'Salaried', 15000000, 999999999, 700, 729, false, 0.081),
    (pid, 'Salaried', 15000000, 999999999, 650, 700, false, 0.086),
    (pid, 'Salaried', 15000000, 999999999, NULL, NULL, true, 0.081);
END IF;

-- L&T — Home Loan SEP/SENP (4 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'LT-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SEP_SENP', 0, 5000000, 800, 900, false, 0.083),
    (pid, 'SEP_SENP', 0, 5000000, 780, 799, false, 0.085),
    (pid, 'SEP_SENP', 0, 5000000, 750, 779, false, 0.085),
    (pid, 'SEP_SENP', 0, 5000000, 730, 749, false, 0.0865),
    (pid, 'SEP_SENP', 0, 5000000, 700, 729, false, 0.0865),
    (pid, 'SEP_SENP', 0, 5000000, 650, 700, false, 0.092),
    (pid, 'SEP_SENP', 0, 5000000, NULL, NULL, true, 0.0865),
    (pid, 'SEP_SENP', 5000000, 10000000, 800, 900, false, 0.0825),
    (pid, 'SEP_SENP', 5000000, 10000000, 780, 799, false, 0.0845),
    (pid, 'SEP_SENP', 5000000, 10000000, 750, 779, false, 0.0845),
    (pid, 'SEP_SENP', 5000000, 10000000, 730, 749, false, 0.086),
    (pid, 'SEP_SENP', 5000000, 10000000, 700, 729, false, 0.086),
    (pid, 'SEP_SENP', 5000000, 10000000, 650, 700, false, 0.091),
    (pid, 'SEP_SENP', 5000000, 10000000, NULL, NULL, true, 0.086),
    (pid, 'SEP_SENP', 10000000, 15000000, 800, 900, false, 0.0825),
    (pid, 'SEP_SENP', 10000000, 15000000, 780, 799, false, 0.083),
    (pid, 'SEP_SENP', 10000000, 15000000, 750, 779, false, 0.083),
    (pid, 'SEP_SENP', 10000000, 15000000, 730, 749, false, 0.0845),
    (pid, 'SEP_SENP', 10000000, 15000000, 700, 729, false, 0.0845),
    (pid, 'SEP_SENP', 10000000, 15000000, 650, 700, false, 0.0895),
    (pid, 'SEP_SENP', 10000000, 15000000, NULL, NULL, true, 0.0845),
    (pid, 'SEP_SENP', 15000000, 999999999, 800, 900, false, 0.082),
    (pid, 'SEP_SENP', 15000000, 999999999, 780, 799, false, 0.0825),
    (pid, 'SEP_SENP', 15000000, 999999999, 750, 779, false, 0.0825),
    (pid, 'SEP_SENP', 15000000, 999999999, 730, 749, false, 0.084),
    (pid, 'SEP_SENP', 15000000, 999999999, 700, 729, false, 0.084),
    (pid, 'SEP_SENP', 15000000, 999999999, 650, 700, false, 0.089),
    (pid, 'SEP_SENP', 15000000, 999999999, NULL, NULL, true, 0.084);
END IF;

-- L&T — LAP Salaried (4 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'LT-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'Salaried', 0, 5000000, 800, 900, false, 0.0795),
    (pid, 'Salaried', 0, 5000000, 780, 799, false, 0.082),
    (pid, 'Salaried', 0, 5000000, 750, 779, false, 0.082),
    (pid, 'Salaried', 0, 5000000, 730, 749, false, 0.0835),
    (pid, 'Salaried', 0, 5000000, 700, 729, false, 0.0835),
    (pid, 'Salaried', 0, 5000000, 650, 700, false, 0.089),
    (pid, 'Salaried', 0, 5000000, NULL, NULL, true, 0.0835),
    (pid, 'Salaried', 5000000, 10000000, 800, 900, false, 0.078),
    (pid, 'Salaried', 5000000, 10000000, 780, 799, false, 0.0815),
    (pid, 'Salaried', 5000000, 10000000, 750, 779, false, 0.0815),
    (pid, 'Salaried', 5000000, 10000000, 730, 749, false, 0.083),
    (pid, 'Salaried', 5000000, 10000000, 700, 729, false, 0.083),
    (pid, 'Salaried', 5000000, 10000000, 650, 700, false, 0.088),
    (pid, 'Salaried', 5000000, 10000000, NULL, NULL, true, 0.083),
    (pid, 'Salaried', 10000000, 15000000, 800, 900, false, 0.078),
    (pid, 'Salaried', 10000000, 15000000, 780, 799, false, 0.08),
    (pid, 'Salaried', 10000000, 15000000, 750, 779, false, 0.08),
    (pid, 'Salaried', 10000000, 15000000, 730, 749, false, 0.0815),
    (pid, 'Salaried', 10000000, 15000000, 700, 729, false, 0.0815),
    (pid, 'Salaried', 10000000, 15000000, 650, 700, false, 0.0865),
    (pid, 'Salaried', 10000000, 15000000, NULL, NULL, true, 0.0815),
    (pid, 'Salaried', 15000000, 999999999, 800, 900, false, 0.0775),
    (pid, 'Salaried', 15000000, 999999999, 780, 799, false, 0.0795),
    (pid, 'Salaried', 15000000, 999999999, 750, 779, false, 0.0795),
    (pid, 'Salaried', 15000000, 999999999, 730, 749, false, 0.081),
    (pid, 'Salaried', 15000000, 999999999, 700, 729, false, 0.081),
    (pid, 'Salaried', 15000000, 999999999, 650, 700, false, 0.086),
    (pid, 'Salaried', 15000000, 999999999, NULL, NULL, true, 0.081);
END IF;

-- L&T — LAP SEP/SENP (4 slabs)
SELECT id INTO pid FROM loan_products WHERE product_code = 'LT-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SEP_SENP', 0, 5000000, 800, 900, false, 0.083),
    (pid, 'SEP_SENP', 0, 5000000, 780, 799, false, 0.085),
    (pid, 'SEP_SENP', 0, 5000000, 750, 779, false, 0.085),
    (pid, 'SEP_SENP', 0, 5000000, 730, 749, false, 0.0865),
    (pid, 'SEP_SENP', 0, 5000000, 700, 729, false, 0.0865),
    (pid, 'SEP_SENP', 0, 5000000, 650, 700, false, 0.092),
    (pid, 'SEP_SENP', 0, 5000000, NULL, NULL, true, 0.0865),
    (pid, 'SEP_SENP', 5000000, 10000000, 800, 900, false, 0.0825),
    (pid, 'SEP_SENP', 5000000, 10000000, 780, 799, false, 0.0845),
    (pid, 'SEP_SENP', 5000000, 10000000, 750, 779, false, 0.0845),
    (pid, 'SEP_SENP', 5000000, 10000000, 730, 749, false, 0.086),
    (pid, 'SEP_SENP', 5000000, 10000000, 700, 729, false, 0.086),
    (pid, 'SEP_SENP', 5000000, 10000000, 650, 700, false, 0.091),
    (pid, 'SEP_SENP', 5000000, 10000000, NULL, NULL, true, 0.086),
    (pid, 'SEP_SENP', 10000000, 15000000, 800, 900, false, 0.0825),
    (pid, 'SEP_SENP', 10000000, 15000000, 780, 799, false, 0.083),
    (pid, 'SEP_SENP', 10000000, 15000000, 750, 779, false, 0.083),
    (pid, 'SEP_SENP', 10000000, 15000000, 730, 749, false, 0.0845),
    (pid, 'SEP_SENP', 10000000, 15000000, 700, 729, false, 0.0845),
    (pid, 'SEP_SENP', 10000000, 15000000, 650, 700, false, 0.0895),
    (pid, 'SEP_SENP', 10000000, 15000000, NULL, NULL, true, 0.0845),
    (pid, 'SEP_SENP', 15000000, 999999999, 800, 900, false, 0.082),
    (pid, 'SEP_SENP', 15000000, 999999999, 780, 799, false, 0.0825),
    (pid, 'SEP_SENP', 15000000, 999999999, 750, 779, false, 0.0825),
    (pid, 'SEP_SENP', 15000000, 999999999, 730, 749, false, 0.084),
    (pid, 'SEP_SENP', 15000000, 999999999, 700, 729, false, 0.084),
    (pid, 'SEP_SENP', 15000000, 999999999, 650, 700, false, 0.089),
    (pid, 'SEP_SENP', 15000000, 999999999, NULL, NULL, true, 0.084);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- SBI — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'SBI-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.075),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0755),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.076),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.081),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.083),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.09),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.081);
END IF;

-- SBI — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'SBI-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0755),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.076),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0765),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0815),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0835),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0905),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0815);
END IF;

-- SBI — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'SBI-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.084),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0845),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.085),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.09),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.092),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.099),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.09);
END IF;

-- SBI — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'SBI-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0845),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.085),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0855),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0905),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0925),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.0995),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0905);
END IF;

-- ═══════════════════════════════════════════════════════════════════════════════
-- YES BANK — Home Loan (Salaried + SEP)
-- ═══════════════════════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'YES-HL-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.0795),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.08),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.0805),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.0855),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.0875),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.0945),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.0855);
END IF;

-- YES BANK — Home Loan (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'YES-HL-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.08),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.0805),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.081),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.086),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.088),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.095),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.086);
END IF;

-- YES BANK — LAP (Salaried + SEP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'YES-LAP-0001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SALARIED_SEP', 0, 999999999, 800, 900, false, 0.088),
    (pid, 'SALARIED_SEP', 0, 999999999, 780, 799, false, 0.0885),
    (pid, 'SALARIED_SEP', 0, 999999999, 750, 779, false, 0.089),
    (pid, 'SALARIED_SEP', 0, 999999999, 730, 749, false, 0.094),
    (pid, 'SALARIED_SEP', 0, 999999999, 700, 729, false, 0.096),
    (pid, 'SALARIED_SEP', 0, 999999999, 650, 700, false, 0.103),
    (pid, 'SALARIED_SEP', 0, 999999999, NULL, NULL, true, 0.094);
END IF;

-- YES BANK — LAP (SENP)
SELECT id INTO pid FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'SENP', 0, 999999999, 800, 900, false, 0.0885),
    (pid, 'SENP', 0, 999999999, 780, 799, false, 0.089),
    (pid, 'SENP', 0, 999999999, 750, 779, false, 0.0895),
    (pid, 'SENP', 0, 999999999, 730, 749, false, 0.0945),
    (pid, 'SENP', 0, 999999999, 700, 729, false, 0.0965),
    (pid, 'SENP', 0, 999999999, 650, 700, false, 0.1035),
    (pid, 'SENP', 0, 999999999, NULL, NULL, true, 0.0945);
END IF;

END $$;
