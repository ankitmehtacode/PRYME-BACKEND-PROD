-- V20: Complete ROI Matrix Seeding
-- All CIBIL-range-based interest rate grids for production lenders

DO $$
DECLARE
    pid BIGINT;
BEGIN

-- ══════════════════════════════════════════════════════════════
-- BAJAJ NEAR PRIME HL — Missing slabs (>75L Salaried, SENP >30L)
-- ══════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-NP-HL';

-- Salaried >75 Lakhs
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'Salaried', 7500001, 30000000, 800, 900, false, 0.0925),
(pid, 'Salaried', 7500001, 30000000, 780, 799, false, 0.0950),
(pid, 'Salaried', 7500001, 30000000, 750, 779, false, 0.0950),
(pid, 'Salaried', 7500001, 30000000, 730, 749, false, 0.0950),
(pid, 'Salaried', 7500001, 30000000, 700, 729, false, 0.0975),
(pid, 'Salaried', 7500001, 30000000, 650, 700, false, 0.1050),
(pid, 'Salaried', 7500001, 30000000, null, null, true, 0.0975);

-- SENP >30-75 Lakhs
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'SENP', 3000001, 7500000, 800, 900, false, 0.0995),
(pid, 'SENP', 3000001, 7500000, 780, 799, false, 0.1020),
(pid, 'SENP', 3000001, 7500000, 750, 779, false, 0.1020),
(pid, 'SENP', 3000001, 7500000, 730, 749, false, 0.1020),
(pid, 'SENP', 3000001, 7500000, 700, 729, false, 0.1040),
(pid, 'SENP', 3000001, 7500000, 650, 700, false, 0.1110),
(pid, 'SENP', 3000001, 7500000, null, null, true, 0.1040);

-- SENP >75 Lakhs
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'SENP', 7500001, 30000000, 800, 900, false, 0.1020),
(pid, 'SENP', 7500001, 30000000, 780, 799, false, 0.1045),
(pid, 'SENP', 7500001, 30000000, 750, 779, false, 0.1045),
(pid, 'SENP', 7500001, 30000000, 730, 749, false, 0.1045),
(pid, 'SENP', 7500001, 30000000, 700, 729, false, 0.1065),
(pid, 'SENP', 7500001, 30000000, 650, 700, false, 0.1135),
(pid, 'SENP', 7500001, 30000000, null, null, true, 0.1065);

-- ══════════════════════════════════════════════════════════════
-- BAJAJ NEAR PRIME LAP — Mirror HL matrix
-- ══════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'BAJAJ-NP-LAP';

INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
-- Salaried <=30L
(pid, 'Salaried', 0, 3000000, 800, 900, false, 0.0925),
(pid, 'Salaried', 0, 3000000, 780, 799, false, 0.0925),
(pid, 'Salaried', 0, 3000000, 750, 779, false, 0.0925),
(pid, 'Salaried', 0, 3000000, 730, 749, false, 0.0925),
(pid, 'Salaried', 0, 3000000, 700, 729, false, 0.0975),
(pid, 'Salaried', 0, 3000000, 650, 700, false, 0.1050),
(pid, 'Salaried', 0, 3000000, null, null, true, 0.0975),
-- Salaried >30-75L
(pid, 'Salaried', 3000001, 7500000, 800, 900, false, 0.0925),
(pid, 'Salaried', 3000001, 7500000, 780, 799, false, 0.0950),
(pid, 'Salaried', 3000001, 7500000, 750, 779, false, 0.0950),
(pid, 'Salaried', 3000001, 7500000, 730, 749, false, 0.0950),
(pid, 'Salaried', 3000001, 7500000, 700, 729, false, 0.0975),
(pid, 'Salaried', 3000001, 7500000, 650, 700, false, 0.1050),
(pid, 'Salaried', 3000001, 7500000, null, null, true, 0.0975),
-- Salaried >75L
(pid, 'Salaried', 7500001, 30000000, 800, 900, false, 0.0925),
(pid, 'Salaried', 7500001, 30000000, 780, 799, false, 0.0950),
(pid, 'Salaried', 7500001, 30000000, 750, 779, false, 0.0950),
(pid, 'Salaried', 7500001, 30000000, 730, 749, false, 0.0950),
(pid, 'Salaried', 7500001, 30000000, 700, 729, false, 0.0975),
(pid, 'Salaried', 7500001, 30000000, 650, 700, false, 0.1050),
(pid, 'Salaried', 7500001, 30000000, null, null, true, 0.0975),
-- SENP <=30L
(pid, 'SENP', 0, 3000000, 800, 900, false, 0.0970),
(pid, 'SENP', 0, 3000000, 780, 799, false, 0.0995),
(pid, 'SENP', 0, 3000000, 750, 779, false, 0.0995),
(pid, 'SENP', 0, 3000000, 730, 749, false, 0.0995),
(pid, 'SENP', 0, 3000000, 700, 729, false, 0.1015),
(pid, 'SENP', 0, 3000000, 650, 700, false, 0.1085),
(pid, 'SENP', 0, 3000000, null, null, true, 0.1015),
-- SENP >30-75L
(pid, 'SENP', 3000001, 7500000, 800, 900, false, 0.0995),
(pid, 'SENP', 3000001, 7500000, 780, 799, false, 0.1020),
(pid, 'SENP', 3000001, 7500000, 750, 779, false, 0.1020),
(pid, 'SENP', 3000001, 7500000, 730, 749, false, 0.1020),
(pid, 'SENP', 3000001, 7500000, 700, 729, false, 0.1040),
(pid, 'SENP', 3000001, 7500000, 650, 700, false, 0.1110),
(pid, 'SENP', 3000001, 7500000, null, null, true, 0.1040),
-- SENP >75L
(pid, 'SENP', 7500001, 30000000, 800, 900, false, 0.1020),
(pid, 'SENP', 7500001, 30000000, 780, 799, false, 0.1045),
(pid, 'SENP', 7500001, 30000000, 750, 779, false, 0.1045),
(pid, 'SENP', 7500001, 30000000, 730, 749, false, 0.1045),
(pid, 'SENP', 7500001, 30000000, 700, 729, false, 0.1065),
(pid, 'SENP', 7500001, 30000000, 650, 700, false, 0.1135),
(pid, 'SENP', 7500001, 30000000, null, null, true, 0.1065);

-- ══════════════════════════════════════════════════════════════
-- L&T HL — Full 4-slab matrix (replacing partial V19 data not needed, additive)
-- Missing slabs: >50-100L, >100-150L, >150L for both Salaried & SEP
-- ══════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'LNT-HL-001';

-- Salaried >50-100L
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'Salaried', 5000001, 10000000, 800, 900, false, 0.0780),
(pid, 'Salaried', 5000001, 10000000, 780, 799, false, 0.0815),
(pid, 'Salaried', 5000001, 10000000, 750, 779, false, 0.0815),
(pid, 'Salaried', 5000001, 10000000, 730, 749, false, 0.0830),
(pid, 'Salaried', 5000001, 10000000, 700, 729, false, 0.0830),
(pid, 'Salaried', 5000001, 10000000, 650, 700, false, 0.0880),
(pid, 'Salaried', 5000001, 10000000, null, null, true, 0.0830);

-- Salaried >100-150L
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'Salaried', 10000001, 15000000, 800, 900, false, 0.0780),
(pid, 'Salaried', 10000001, 15000000, 780, 799, false, 0.0800),
(pid, 'Salaried', 10000001, 15000000, 750, 779, false, 0.0800),
(pid, 'Salaried', 10000001, 15000000, 730, 749, false, 0.0815),
(pid, 'Salaried', 10000001, 15000000, 700, 729, false, 0.0815),
(pid, 'Salaried', 10000001, 15000000, 650, 700, false, 0.0865),
(pid, 'Salaried', 10000001, 15000000, null, null, true, 0.0815);

-- Salaried >150L
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'Salaried', 15000001, 50000000, 800, 900, false, 0.0775),
(pid, 'Salaried', 15000001, 50000000, 780, 799, false, 0.0795),
(pid, 'Salaried', 15000001, 50000000, 750, 779, false, 0.0795),
(pid, 'Salaried', 15000001, 50000000, 730, 749, false, 0.0810),
(pid, 'Salaried', 15000001, 50000000, 700, 729, false, 0.0810),
(pid, 'Salaried', 15000001, 50000000, 650, 700, false, 0.0860),
(pid, 'Salaried', 15000001, 50000000, null, null, true, 0.0810);

-- SEP >50-100L
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'SEP', 5000001, 10000000, 800, 900, false, 0.0825),
(pid, 'SEP', 5000001, 10000000, 780, 799, false, 0.0845),
(pid, 'SEP', 5000001, 10000000, 750, 779, false, 0.0845),
(pid, 'SEP', 5000001, 10000000, 730, 749, false, 0.0860),
(pid, 'SEP', 5000001, 10000000, 700, 729, false, 0.0860),
(pid, 'SEP', 5000001, 10000000, 650, 700, false, 0.0910),
(pid, 'SEP', 5000001, 10000000, null, null, true, 0.0860);

-- SEP >100-150L
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'SEP', 10000001, 15000000, 800, 900, false, 0.0825),
(pid, 'SEP', 10000001, 15000000, 780, 799, false, 0.0830),
(pid, 'SEP', 10000001, 15000000, 750, 779, false, 0.0830),
(pid, 'SEP', 10000001, 15000000, 730, 749, false, 0.0845),
(pid, 'SEP', 10000001, 15000000, 700, 729, false, 0.0845),
(pid, 'SEP', 10000001, 15000000, 650, 700, false, 0.0895),
(pid, 'SEP', 10000001, 15000000, null, null, true, 0.0845);

-- SEP >150L
INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
(pid, 'SEP', 15000001, 50000000, 800, 900, false, 0.0820),
(pid, 'SEP', 15000001, 50000000, 780, 799, false, 0.0825),
(pid, 'SEP', 15000001, 50000000, 750, 779, false, 0.0825),
(pid, 'SEP', 15000001, 50000000, 730, 749, false, 0.0840),
(pid, 'SEP', 15000001, 50000000, 700, 729, false, 0.0840),
(pid, 'SEP', 15000001, 50000000, 650, 700, false, 0.0890),
(pid, 'SEP', 15000001, 50000000, null, null, true, 0.0840);

-- ══════════════════════════════════════════════════════════════
-- L&T LAP — Mirror HL matrix
-- ══════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'LNT-LAP-001';

INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
-- Salaried 0-50L
(pid, 'Salaried', 0, 5000000, 800, 900, false, 0.0795),
(pid, 'Salaried', 0, 5000000, 780, 799, false, 0.0820),
(pid, 'Salaried', 0, 5000000, 750, 779, false, 0.0820),
(pid, 'Salaried', 0, 5000000, 730, 749, false, 0.0835),
(pid, 'Salaried', 0, 5000000, 700, 729, false, 0.0835),
(pid, 'Salaried', 0, 5000000, 650, 700, false, 0.0890),
(pid, 'Salaried', 0, 5000000, null, null, true, 0.0835),
-- Salaried >50-100L
(pid, 'Salaried', 5000001, 10000000, 800, 900, false, 0.0780),
(pid, 'Salaried', 5000001, 10000000, 780, 799, false, 0.0815),
(pid, 'Salaried', 5000001, 10000000, 750, 779, false, 0.0815),
(pid, 'Salaried', 5000001, 10000000, 730, 749, false, 0.0830),
(pid, 'Salaried', 5000001, 10000000, 700, 729, false, 0.0830),
(pid, 'Salaried', 5000001, 10000000, 650, 700, false, 0.0880),
(pid, 'Salaried', 5000001, 10000000, null, null, true, 0.0830),
-- Salaried >100-150L
(pid, 'Salaried', 10000001, 15000000, 800, 900, false, 0.0780),
(pid, 'Salaried', 10000001, 15000000, 780, 799, false, 0.0800),
(pid, 'Salaried', 10000001, 15000000, 750, 779, false, 0.0800),
(pid, 'Salaried', 10000001, 15000000, 730, 749, false, 0.0815),
(pid, 'Salaried', 10000001, 15000000, 700, 729, false, 0.0815),
(pid, 'Salaried', 10000001, 15000000, 650, 700, false, 0.0865),
(pid, 'Salaried', 10000001, 15000000, null, null, true, 0.0815),
-- Salaried >150L
(pid, 'Salaried', 15000001, 50000000, 800, 900, false, 0.0775),
(pid, 'Salaried', 15000001, 50000000, 780, 799, false, 0.0795),
(pid, 'Salaried', 15000001, 50000000, 750, 779, false, 0.0795),
(pid, 'Salaried', 15000001, 50000000, 730, 749, false, 0.0810),
(pid, 'Salaried', 15000001, 50000000, 700, 729, false, 0.0810),
(pid, 'Salaried', 15000001, 50000000, 650, 700, false, 0.0860),
(pid, 'Salaried', 15000001, 50000000, null, null, true, 0.0810),
-- SEP 0-50L
(pid, 'SEP', 0, 5000000, 800, 900, false, 0.0830),
(pid, 'SEP', 0, 5000000, 780, 799, false, 0.0850),
(pid, 'SEP', 0, 5000000, 750, 779, false, 0.0850),
(pid, 'SEP', 0, 5000000, 730, 749, false, 0.0865),
(pid, 'SEP', 0, 5000000, 700, 729, false, 0.0865),
(pid, 'SEP', 0, 5000000, 650, 700, false, 0.0920),
(pid, 'SEP', 0, 5000000, null, null, true, 0.0865),
-- SEP >50-100L
(pid, 'SEP', 5000001, 10000000, 800, 900, false, 0.0825),
(pid, 'SEP', 5000001, 10000000, 780, 799, false, 0.0845),
(pid, 'SEP', 5000001, 10000000, 750, 779, false, 0.0845),
(pid, 'SEP', 5000001, 10000000, 730, 749, false, 0.0860),
(pid, 'SEP', 5000001, 10000000, 700, 729, false, 0.0860),
(pid, 'SEP', 5000001, 10000000, 650, 700, false, 0.0910),
(pid, 'SEP', 5000001, 10000000, null, null, true, 0.0860),
-- SEP >100-150L
(pid, 'SEP', 10000001, 15000000, 800, 900, false, 0.0825),
(pid, 'SEP', 10000001, 15000000, 780, 799, false, 0.0830),
(pid, 'SEP', 10000001, 15000000, 750, 779, false, 0.0830),
(pid, 'SEP', 10000001, 15000000, 730, 749, false, 0.0845),
(pid, 'SEP', 10000001, 15000000, 700, 729, false, 0.0845),
(pid, 'SEP', 10000001, 15000000, 650, 700, false, 0.0895),
(pid, 'SEP', 10000001, 15000000, null, null, true, 0.0845),
-- SEP >150L
(pid, 'SEP', 15000001, 50000000, 800, 900, false, 0.0820),
(pid, 'SEP', 15000001, 50000000, 780, 799, false, 0.0825),
(pid, 'SEP', 15000001, 50000000, 750, 779, false, 0.0825),
(pid, 'SEP', 15000001, 50000000, 730, 749, false, 0.0840),
(pid, 'SEP', 15000001, 50000000, 700, 729, false, 0.0840),
(pid, 'SEP', 15000001, 50000000, 650, 700, false, 0.0890),
(pid, 'SEP', 15000001, 50000000, null, null, true, 0.0840);

-- ══════════════════════════════════════════════════════════════
-- HDFC HL — No loan amount slabs, flat per CIBIL band
-- ══════════════════════════════════════════════════════════════
SELECT id INTO pid FROM loan_products WHERE product_code = 'HDFC-HL-001';
IF pid IS NOT NULL THEN
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (pid, 'Salaried', 0, 9999999999, 800, 900, false, 0.0715),
    (pid, 'Salaried', 0, 9999999999, 780, 799, false, 0.0720),
    (pid, 'Salaried', 0, 9999999999, 750, 779, false, 0.0725),
    (pid, 'Salaried', 0, 9999999999, 730, 749, false, 0.0785),
    (pid, 'Salaried', 0, 9999999999, 700, 729, false, 0.0795),
    (pid, 'Salaried', 0, 9999999999, 650, 700, false, 0.0875),
    (pid, 'Salaried', 0, 9999999999, null, null, true, 0.0785),
    (pid, 'SENP', 0, 9999999999, 800, 900, false, 0.0720),
    (pid, 'SENP', 0, 9999999999, 780, 799, false, 0.0725),
    (pid, 'SENP', 0, 9999999999, 750, 779, false, 0.0735),
    (pid, 'SENP', 0, 9999999999, 730, 749, false, 0.0795),
    (pid, 'SENP', 0, 9999999999, 700, 729, false, 0.0805),
    (pid, 'SENP', 0, 9999999999, 650, 700, false, 0.0885),
    (pid, 'SENP', 0, 9999999999, null, null, true, 0.0795);
END IF;

END $$;
