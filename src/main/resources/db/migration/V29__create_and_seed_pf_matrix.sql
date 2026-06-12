-- ═══════════════════════════════════════════════════════════════════════════════
-- V29 — REGISTER NEW PARTNERS & PRODUCTS, CREATE PF MATRIX, SEED SLABS
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. Register new banks in partners
INSERT INTO banks (bank_name, active, lender_code)
VALUES 
('JIO Finance', true, 206),
('IDBI', true, 207),
('Tata Capital', true, 208),
('IDFC', true, 209)
ON CONFLICT (bank_name) DO NOTHING;

-- 2. Ingest new loan products for missing partners
INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, min_loan_amount, max_loan_amount, min_tenure_months, max_tenure_months, is_active, processing_fee, login_fees)
VALUES
('JIO-HL-0001', 'Jio Home Loan Salaried/SEP', 206, 'JIO Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0025, 0),
('JIO-HL-0002', 'Jio Home Loan SENP', 206, 'JIO Finance', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0025, 0),
('JIO-LAP-0001', 'Jio LAP Salaried/SEP', 206, 'JIO Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0025, 0),
('JIO-LAP-0002', 'Jio LAP SENP', 206, 'JIO Finance', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0025, 0),

('IDBI-HL-0001', 'IDBI Home Loan Salaried/SEP', 207, 'IDBI', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 0),
('IDBI-HL-0002', 'IDBI Home Loan SENP', 207, 'IDBI', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0100, 0),
('IDBI-LAP-0001', 'IDBI LAP Salaried/SEP', 207, 'IDBI', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0070, 0),
('IDBI-LAP-0002', 'IDBI LAP SENP', 207, 'IDBI', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0070, 0),

('TATA-HL-0001', 'Tata Capital Home Loan Salaried/SEP', 208, 'Tata Capital', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0025, 0),
('TATA-HL-0002', 'Tata Capital Home Loan SENP', 208, 'Tata Capital', 'HL', 'Floating', 650, 900, 0.0850, 100000, 999999999, 12, 360, true, 0.0025, 0),
('TATA-LAP-0001', 'Tata Capital LAP Salaried/SEP', 208, 'Tata Capital', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0100, 0),
('TATA-LAP-0002', 'Tata Capital LAP SENP', 208, 'Tata Capital', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0100, 0),

('IDFC-LAP-0001', 'IDFC LAP Salaried/SEP', 209, 'IDFC', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0050, 0),
('IDFC-LAP-0002', 'IDFC LAP SENP', 209, 'IDFC', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0050, 0),

('ICICIHFC-LAP-0001', 'ICICI HFC LAP Salaried/SEP', 204, 'ICICI HFC', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0100, 0),
('ICICIHFC-LAP-0002', 'ICICI HFC LAP SENP', 204, 'ICICI HFC', 'LAP', 'Floating', 650, 900, 0.0900, 100000, 999999999, 12, 360, true, 0.0100, 0)
ON CONFLICT (product_code) DO NOTHING;

-- 3. Ingest default eligibility conditions for the new products
INSERT INTO eligibility_conditions (product_id, product_code, employment_type, min_age, max_age, min_income, work_exp_years, business_age_years, cibil_min, property_type, bank_name, loan_type, is_active, foir_max, ltv_allowed)
SELECT 
    id, 
    product_code,
    CASE 
        WHEN product_code LIKE '%-0001' THEN 'Salaried'
        ELSE 'SEP/SENP'
    END,
    21, 65, 25000,
    CASE 
        WHEN product_code LIKE '%-0001' THEN 1
        ELSE NULL
    END,
    CASE 
        WHEN product_code LIKE '%-0001' THEN NULL
        ELSE 3
    END,
    650,
    'RESIDENTIAL, COMMERCIAL, PLOT',
    lender_name,
    loan_type,
    true,
    0.65, 
    0.80 
FROM loan_products
WHERE product_code LIKE 'JIO-%' OR product_code LIKE 'IDBI-%' OR product_code LIKE 'TATA-%' OR product_code LIKE 'IDFC-%' OR product_code LIKE 'ICICIHFC-%'
ON CONFLICT DO NOTHING;

-- 4. Create product_pf_matrix table
CREATE TABLE IF NOT EXISTS product_pf_matrix (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES loan_products(id) ON DELETE CASCADE,
    employment_type VARCHAR(50) NOT NULL,
    min_loan_amount NUMERIC(15, 2) DEFAULT 0,
    max_loan_amount NUMERIC(15, 2) DEFAULT 999999999,
    fee_value NUMERIC(14, 4) NOT NULL,
    is_flat BOOLEAN DEFAULT FALSE,
    min_fee NUMERIC(15, 2),
    max_fee NUMERIC(15, 2),
    tax_rate NUMERIC(5, 4) DEFAULT 0.1800,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Seed dynamic processing fee slabs
DO $$
DECLARE
    p_id BIGINT;
BEGIN

-- L&T Finance
SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 2000000.00, 20000000.00, 10000.00, true, 0.1800),
    (p_id, 'SALARIED_SEP', 20000001.00, 50000000.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 50000000.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 2000000.00, 50000000.00, 0.0100, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 50000000.00, 0.0100, false, 0.1800);
END IF;

-- ICICI Bank
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 2000000.00, 999999999.00, 0.0030, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 999999999.00, 0.0030, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 2000000.00, 999999999.00, 0.0060, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 999999999.00, 0.0060, false, 0.1800);
END IF;

-- Bandhan Bank
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 200000.00, 80000000.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 200000.00, 80000000.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 200000.00, 80000000.00, 0.0100, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 200000.00, 80000000.00, 0.0100, false, 0.1800);
END IF;

-- Aditya Birla Finance Limited
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 3500000.00, 75000000.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 75000000.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 3500000.00, 75000000.00, 0.0075, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 75000000.00, 0.0075, false, 0.1800);
END IF;

-- Bank of Baroda
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 500000.00, 999999999.00, 0.0000, false, 0.0000);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 500000.00, 999999999.00, 0.0000, false, 0.0000);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, min_fee, max_fee, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 500000.00, 999999999.00, 0.0100, false, 8500.00, 150000.00, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, min_fee, max_fee, tax_rate) VALUES
    (p_id, 'SEP_SENP', 500000.00, 999999999.00, 0.0100, false, 8500.00, 150000.00, 0.1800);
END IF;

-- SBI
SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, min_fee, max_fee, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 1000000.00, 999999999.00, 0.0035, false, 5000.00, 15000.00, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, min_fee, max_fee, tax_rate) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 999999999.00, 0.0035, false, 5000.00, 20000.00, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, max_fee, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 1000000.00, 999999999.00, 0.0100, false, 60000.00, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, max_fee, tax_rate) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 999999999.00, 0.0100, false, 60000.00, 0.1800);
END IF;

-- Bajaj Prime (Bajaj Finance)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 1000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP', 1000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0003';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP (Industry Margin)', 1000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 1000000.00, 999999999.00, 0.0075, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP', 1000000.00, 999999999.00, 0.0075, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0003';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP (Industry Margin)', 1000000.00, 999999999.00, 0.0075, false, 0.1800);
END IF;

-- Yes Bank (YES BANK)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 2100000.00, 999999999.00, 0.0030, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP', 2100000.00, 999999999.00, 0.0030, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 2100000.00, 999999999.00, 0.0075, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP', 2100000.00, 999999999.00, 0.0075, false, 0.1800);
END IF;

-- HDFC (HDFC Bank)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 0.00, 1200000.00, 4000.00, true, 0.1800),
    (p_id, 'SALARIED_SEP', 1200001.00, 2000000.00, 6000.00, true, 0.1800),
    (p_id, 'SALARIED_SEP', 2000001.00, 999999999.00, 10000.00, true, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, max_fee, tax_rate) VALUES
    (p_id, 'SENP', 0.00, 20000000.00, 0.0020, false, 20000.00, 0.1800),
    (p_id, 'SENP', 20000001.00, 50000000.00, 0.0020, false, 40000.00, 0.1800),
    (p_id, 'SENP', 50000001.00, 999999999.00, 0.0020, false, 50000.00, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SALARIED_SEP', 0.00, 999999999.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP', 50000000.00, 999999999.00, 0.0050, false, 0.1800);
END IF;

-- JIO Finance
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 3000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 3000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 3000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 3000000.00, 999999999.00, 0.0025, false, 0.1800);
END IF;

-- IDBI
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 5000000.00, 7500000.00, 10000.00, true, 0.1800),
    (p_id, 'Salaried', 7500001.00, 999999999.00, 15000.00, true, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SENP', 5000000.00, 999999999.00, 11000.00, true, 0.1800),
    (p_id, 'SEP', 5000000.00, 999999999.00, 17500.00, true, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 5000000.00, 999999999.00, 0.0070, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 999999999.00, 0.0070, false, 0.1800);
END IF;

-- Tata Capital
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 1000000.00, 100000000.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 100000000.00, 0.0025, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 1000000.00, 100000000.00, 0.0100, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 100000000.00, 0.0100, false, 0.1800);
END IF;

-- IDFC
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'Salaried', 5000000.00, 100000000.00, 0.0050, false, 0.1800);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 100000000.00, 0.0050, false, 0.1800);
END IF;

-- ICICI HFC
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICIHFC-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 1500000.00, 100000000.00, 1000.00, true, 0.1800);
END IF;

END $$;
