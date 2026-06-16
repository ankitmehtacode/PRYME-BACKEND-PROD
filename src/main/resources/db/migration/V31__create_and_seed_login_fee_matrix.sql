-- ═══════════════════════════════════════════════════════════════════════════════
-- V31 — CREATE AND SEED DYNAMIC LOGIN FEE MATRIX
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. Create product_login_fee_matrix table
CREATE TABLE IF NOT EXISTS product_login_fee_matrix (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES loan_products(id) ON DELETE CASCADE,
    employment_type VARCHAR(50) NOT NULL,
    min_loan_amount NUMERIC(15, 2) DEFAULT 0,
    max_loan_amount NUMERIC(15, 2) DEFAULT 999999999,
    login_fee NUMERIC(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookup by product
CREATE INDEX IF NOT EXISTS idx_product_login_fee_matrix_product_id ON product_login_fee_matrix(product_id);

-- 2. Discard/Clear existing static login_fees in loan_products to prevent stale fallbacks
UPDATE loan_products SET login_fees = NULL;

-- 3. Seed dynamic login fee slabs
DO $$
DECLARE
    p_id BIGINT;
BEGIN

-- ─────────────────────────────────────────────────────────────────────────────
-- HOME LOANS (HL)
-- ─────────────────────────────────────────────────────────────────────────────

-- L&T Finance HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 2000000.00, 50000000.00, 1000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 50000000.00, 1000.00);
END IF;

-- ICICI Bank HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 2000000.00, 999999999.00, 3000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 999999999.00, 3000.00);
END IF;

-- Bandhan Bank HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 200000.00, 80000000.00, 2360.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 200000.00, 80000000.00, 2360.00);
END IF;

-- Aditya Birla Finance Limited HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 3500000.00, 75000000.00, 2950.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 75000000.00, 2950.00);
END IF;

-- Bank of Baroda HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 500000.00, 999999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 500000.00, 999999999.00, 0.00);
END IF;

-- SBI HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 1000000.00, 999999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 999999999.00, 0.00);
END IF;

-- Bajaj Prime HL (Maps to lender_name 'Bajaj Finance')
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 3500000.00, 100000000.00, 2000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 100000000.00, 3500.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0003';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SENP (Industry Margin)', 3500000.00, 100000000.00, 3500.00);
END IF;

-- HDFC Bank HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 1100000.00, 999999999.00, 4000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1100000.00, 999999999.00, 4000.00);
END IF;

-- JIO Finance HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 3000000.00, 500000000.00, 1500.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3000000.00, 500000000.00, 1500.00);
END IF;

-- IDBI HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 5000000.00, 999999999.00, 2950.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 999999999.00, 2950.00);
END IF;

-- Tata Capital HL
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 1000000.00, 100000000.00, 1000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 100000000.00, 1000.00);
END IF;


-- ─────────────────────────────────────────────────────────────────────────────
-- LOAN AGAINST PROPERTY (LAP)
-- ─────────────────────────────────────────────────────────────────────────────

-- L&T Finance LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 2000000.00, 50000000.00, 1000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 50000000.00, 1000.00);
END IF;

-- ICICI Bank LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 2000000.00, 999999999.00, 5000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 999999999.00, 5000.00);
END IF;

-- Bandhan Bank LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 200000.00, 80000000.00, 2360.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 200000.00, 80000000.00, 2360.00);
END IF;

-- Aditya Birla Finance Limited LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 3500000.00, 75000000.00, 5900.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 75000000.00, 5900.00);
END IF;

-- Bank of Baroda LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 500000.00, 999999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 500000.00, 999999999.00, 0.00);
END IF;

-- SBI LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 1000000.00, 999999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 999999999.00, 0.00);
END IF;

-- Bajaj Prime LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 3500000.00, 100000000.00, 2000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 100000000.00, 3500.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0003';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SENP (Industry Margin)', 3500000.00, 100000000.00, 3500.00);
END IF;

-- IDFC LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 5000000.00, 100000000.00, 5900.00);
END IF;

-- JIO Finance LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 3000000.00, 500000000.00, 3250.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3000000.00, 500000000.00, 1500.00);
END IF;

-- IDBI LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 5000000.00, 999999999.00, 11800.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 999999999.00, 11800.00);
END IF;

-- Tata Capital LAP
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 1000000.00, 100000000.00, 1180.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 100000000.00, 1180.00);
END IF;

END $$;
