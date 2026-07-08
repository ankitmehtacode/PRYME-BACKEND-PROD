-- ═══════════════════════════════════════════════════════════════════════════════
-- V53 — RESEED LOGIN FEE MATRIX WITH UPDATED CLIENT DATA
-- ═══════════════════════════════════════════════════════════════════════════════
-- Source: Updated login fees sheet (July 2026)
-- Strategy: TRUNCATE + RESEED — wipe old V31 data and insert fresh values.
--
-- Employment type mapping:
--   "Salaried" rows      → product_code XXX-HL-0001 / XXX-LAP-0001 (Salaried/SEP variant)
--   "SEP/SENP" rows      → product_code XXX-HL-0002 / XXX-LAP-0002 (SENP variant)
--   When both employment types share the same login fee, we still insert
--   separate rows for each product_code to keep the engine lookup simple.
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. Clear all existing login fee data
TRUNCATE TABLE product_login_fee_matrix;

-- 2. Reseed
DO $$
DECLARE
    p_id BIGINT;
BEGIN

-- ═════════════════════════════════════════════════════════════════════════════
-- HOME LOANS (HL)
-- ═════════════════════════════════════════════════════════════════════════════

-- ── L&T Finance HL ──────────────────────────────────────────────────────────
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

-- ── ICICI Bank HL ───────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 2000000.00, 9999999.00, 3000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 9999999.00, 3000.00);
END IF;

-- ── Bandhan Bank HL ─────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 200000.00, 80000000.00, 2360.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 200000.00, 80000000.00, 2360.00);
END IF;

-- ── Aditya Birla Finance Limited HL ─────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 3500000.00, 75000000.00, 2950.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 75000000.00, 2950.00);
END IF;

-- ── Bank of Baroda HL ───────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 500000.00, 9999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 500000.00, 9999999.00, 0.00);
END IF;

-- ── SBI HL ──────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 1000000.00, 9999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 9999999.00, 0.00);
END IF;

-- ── Bajaj Prime HL ──────────────────────────────────────────────────────────
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

-- ── Yes Bank HL ─────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 2100000.00, 150000000.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2100000.00, 150000000.00, 0.00);
END IF;

-- ── HDFC HL ─────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 1100000.00, 9999999.00, 4000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1100000.00, 9999999.00, 4000.00);
END IF;

-- ── JIO Finance HL ──────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 3000000.00, 500000000.00, 1500.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3000000.00, 500000000.00, 1500.00);
END IF;

-- ── IDBI HL ─────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 5000000.00, 9999999.00, 2950.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 9999999.00, 2950.00);
END IF;

-- ── TATA Capital HL ─────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 1000000.00, 100000000.00, 1000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 100000000.00, 1000.00);
END IF;


-- ═════════════════════════════════════════════════════════════════════════════
-- LOAN AGAINST PROPERTY (LAP)
-- ═════════════════════════════════════════════════════════════════════════════

-- ── L&T Finance LAP ─────────────────────────────────────────────────────────
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

-- ── ICICI Bank LAP ──────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 2000000.00, 9999999.00, 5000.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ICICI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2000000.00, 9999999.00, 5000.00);
END IF;

-- ── Bandhan Bank LAP ────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 200000.00, 80000000.00, 2360.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 200000.00, 80000000.00, 2360.00);
END IF;

-- ── Aditya Birla Finance Limited LAP ────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 3500000.00, 75000000.00, 5900.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 3500000.00, 75000000.00, 5900.00);
END IF;

-- ── Bank of Baroda LAP ──────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 500000.00, 9999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BOB-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 500000.00, 9999999.00, 0.00);
END IF;

-- ── SBI LAP ─────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 1000000.00, 9999999.00, 0.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'SBI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 9999999.00, 0.00);
END IF;

-- ── Bajaj Prime LAP ─────────────────────────────────────────────────────────
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

-- ── Yes Bank LAP ────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 2100000.00, 150000000.00, 2340.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 2100000.00, 150000000.00, 2340.00);
END IF;

-- ── HDFC LAP ────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 1100000.00, 9999999.00, 5900.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1100000.00, 9999999.00, 5900.00);
END IF;

-- ── IDFC LAP ────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 5000000.00, 100000000.00, 5900.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 100000000.00, 5900.00);
END IF;

-- ── JIO Finance LAP ────────────────────────────────────────────────────────
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

-- ── IDBI LAP ────────────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 5000000.00, 9999999.00, 11800.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDBI-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 5000000.00, 9999999.00, 11800.00);
END IF;

-- ── TATA Capital LAP ────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'Salaried', 1000000.00, 100000000.00, 1180.00);
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 1000000.00, 100000000.00, 1180.00);
END IF;

END $$;

-- 3. Verify row count
-- Expected: 48 rows (24 HL + 24 LAP, 1 per product_code × employment_type)
-- Plus 2 IDFC LAP rows = 50 total
DO $$
BEGIN
    RAISE NOTICE 'Login fee matrix reseeded. Row count: %', (SELECT COUNT(*) FROM product_login_fee_matrix);
END $$;
