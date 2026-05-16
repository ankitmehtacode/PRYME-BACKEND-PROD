-- V22: Exact ROI Matrix based on provided spreadsheet

DO $$
DECLARE
    p_bajaj_np_hl BIGINT;
    p_bajaj_np_lap BIGINT;
    p_hdfc_hl BIGINT;
    p_lnt_hl BIGINT;
    p_lnt_lap BIGINT;
BEGIN

SELECT id INTO p_bajaj_np_hl FROM loan_products WHERE product_code = 'BAJAJ-NP-HL';
SELECT id INTO p_bajaj_np_lap FROM loan_products WHERE product_code = 'BAJAJ-NP-LAP';
SELECT id INTO p_hdfc_hl FROM loan_products WHERE product_code = 'HDFC-HL-001';
SELECT id INTO p_lnt_hl FROM loan_products WHERE product_code = 'LNT-HL-001';
SELECT id INTO p_lnt_lap FROM loan_products WHERE product_code = 'LNT-LAP-001';

-- Clear existing mappings
DELETE FROM product_roi_matrix WHERE product_id IN (
    COALESCE(p_bajaj_np_hl, -1),
    COALESCE(p_bajaj_np_lap, -1),
    COALESCE(p_hdfc_hl, -1),
    COALESCE(p_lnt_hl, -1),
    COALESCE(p_lnt_lap, -1)
);

-- ==========================================
-- BAJAJ NEAR PRIME (HL and LAP mirror)
-- ==========================================
IF p_bajaj_np_hl IS NOT NULL THEN
    -- Salaried <=30 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 800, 900, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 780, 799, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 750, 779, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 730, 749, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 700, 729, false, 0.0975),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 650, 700, false, 0.1050),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, null, null, true, 0.0975);

    -- Salaried >30-75 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 800, 900, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 780, 799, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 750, 779, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 730, 749, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 700, 729, false, 0.0975),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 650, 700, false, 0.1050),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, null, null, true, 0.0975);

    -- Salaried >75 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, 800, 900, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, 780, 799, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, 750, 779, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, 730, 749, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, 700, 729, false, 0.0975),
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, 650, 700, false, 0.1050),
    (p_bajaj_np_hl, 'Salaried', 7500001, 30000000, null, null, true, 0.0975);

    -- SENP <=30 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 800, 900, false, 0.0970),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 780, 799, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 750, 779, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 730, 749, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 700, 729, false, 0.1015),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 650, 700, false, 0.1085),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, null, null, true, 0.1015);

    -- SENP >30-75 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, 800, 900, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, 780, 799, false, 0.1020),
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, 750, 779, false, 0.1020),
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, 730, 749, false, 0.1020),
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, 700, 729, false, 0.1040),
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, 650, 700, false, 0.1110),
    (p_bajaj_np_hl, 'SENP', 3000001, 7500000, null, null, true, 0.1040);

    -- SENP >75 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, 800, 900, false, 0.1020),
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, 780, 799, false, 0.1045),
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, 750, 779, false, 0.1045),
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, 730, 749, false, 0.1045),
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, 700, 729, false, 0.1065),
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, 650, 700, false, 0.1135),
    (p_bajaj_np_hl, 'SENP', 7500001, 30000000, null, null, true, 0.1065);

    -- (Optional) Note: "SENP (Industry Margin)" is skipped as it corresponds to a separate product definition if required,
    -- or we use SENP for the main product. The spreadsheet lists it alongside SENP. We map the standard SENP.
END IF;

IF p_bajaj_np_lap IS NOT NULL THEN
    -- Copy same structure for LAP by re-selecting the exact matrix
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi)
    SELECT p_bajaj_np_lap, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi
    FROM product_roi_matrix WHERE product_id = p_bajaj_np_hl;
END IF;

-- ==========================================
-- HDFC
-- ==========================================
IF p_hdfc_hl IS NOT NULL THEN
    -- Salaried/ SEP
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_hdfc_hl, 'Salaried', 0, 9999999999, 800, 900, false, 0.0715),
    (p_hdfc_hl, 'Salaried', 0, 9999999999, 780, 799, false, 0.0720),
    (p_hdfc_hl, 'Salaried', 0, 9999999999, 750, 779, false, 0.0725),
    (p_hdfc_hl, 'Salaried', 0, 9999999999, 730, 749, false, 0.0785),
    (p_hdfc_hl, 'Salaried', 0, 9999999999, 700, 729, false, 0.0795),
    (p_hdfc_hl, 'Salaried', 0, 9999999999, 650, 700, false, 0.0875),
    (p_hdfc_hl, 'Salaried', 0, 9999999999, null, null, true, 0.0785),
    (p_hdfc_hl, 'SEP', 0, 9999999999, 800, 900, false, 0.0715),
    (p_hdfc_hl, 'SEP', 0, 9999999999, 780, 799, false, 0.0720),
    (p_hdfc_hl, 'SEP', 0, 9999999999, 750, 779, false, 0.0725),
    (p_hdfc_hl, 'SEP', 0, 9999999999, 730, 749, false, 0.0785),
    (p_hdfc_hl, 'SEP', 0, 9999999999, 700, 729, false, 0.0795),
    (p_hdfc_hl, 'SEP', 0, 9999999999, 650, 700, false, 0.0875),
    (p_hdfc_hl, 'SEP', 0, 9999999999, null, null, true, 0.0785);

    -- SENP
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_hdfc_hl, 'SENP', 0, 9999999999, 800, 900, false, 0.0720),
    (p_hdfc_hl, 'SENP', 0, 9999999999, 780, 799, false, 0.0725),
    (p_hdfc_hl, 'SENP', 0, 9999999999, 750, 779, false, 0.0735),
    (p_hdfc_hl, 'SENP', 0, 9999999999, 730, 749, false, 0.0795),
    (p_hdfc_hl, 'SENP', 0, 9999999999, 700, 729, false, 0.0805),
    (p_hdfc_hl, 'SENP', 0, 9999999999, 650, 700, false, 0.0885),
    (p_hdfc_hl, 'SENP', 0, 9999999999, null, null, true, 0.0795);
END IF;

-- ==========================================
-- L&T
-- ==========================================
IF p_lnt_hl IS NOT NULL THEN
    -- Salaried 0-50 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'Salaried', 0, 5000000, 800, 900, false, 0.0795),
    (p_lnt_hl, 'Salaried', 0, 5000000, 780, 799, false, 0.0820),
    (p_lnt_hl, 'Salaried', 0, 5000000, 750, 779, false, 0.0820),
    (p_lnt_hl, 'Salaried', 0, 5000000, 730, 749, false, 0.0835),
    (p_lnt_hl, 'Salaried', 0, 5000000, 700, 729, false, 0.0835),
    (p_lnt_hl, 'Salaried', 0, 5000000, 650, 700, false, 0.0890),
    (p_lnt_hl, 'Salaried', 0, 5000000, null, null, true, 0.0835);

    -- Salaried >50-100 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'Salaried', 5000001, 10000000, 800, 900, false, 0.0780),
    (p_lnt_hl, 'Salaried', 5000001, 10000000, 780, 799, false, 0.0815),
    (p_lnt_hl, 'Salaried', 5000001, 10000000, 750, 779, false, 0.0815),
    (p_lnt_hl, 'Salaried', 5000001, 10000000, 730, 749, false, 0.0830),
    (p_lnt_hl, 'Salaried', 5000001, 10000000, 700, 729, false, 0.0830),
    (p_lnt_hl, 'Salaried', 5000001, 10000000, 650, 700, false, 0.0880),
    (p_lnt_hl, 'Salaried', 5000001, 10000000, null, null, true, 0.0830);

    -- Salaried >100-150 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'Salaried', 10000001, 15000000, 800, 900, false, 0.0780),
    (p_lnt_hl, 'Salaried', 10000001, 15000000, 780, 799, false, 0.0800),
    (p_lnt_hl, 'Salaried', 10000001, 15000000, 750, 779, false, 0.0800),
    (p_lnt_hl, 'Salaried', 10000001, 15000000, 730, 749, false, 0.0815),
    (p_lnt_hl, 'Salaried', 10000001, 15000000, 700, 729, false, 0.0815),
    (p_lnt_hl, 'Salaried', 10000001, 15000000, 650, 700, false, 0.0865),
    (p_lnt_hl, 'Salaried', 10000001, 15000000, null, null, true, 0.0815);

    -- Salaried >150 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'Salaried', 15000001, 50000000, 800, 900, false, 0.0775),
    (p_lnt_hl, 'Salaried', 15000001, 50000000, 780, 799, false, 0.0795),
    (p_lnt_hl, 'Salaried', 15000001, 50000000, 750, 779, false, 0.0795),
    (p_lnt_hl, 'Salaried', 15000001, 50000000, 730, 749, false, 0.0810),
    (p_lnt_hl, 'Salaried', 15000001, 50000000, 700, 729, false, 0.0810),
    (p_lnt_hl, 'Salaried', 15000001, 50000000, 650, 700, false, 0.0860),
    (p_lnt_hl, 'Salaried', 15000001, 50000000, null, null, true, 0.0810);

    -- SEP/ SENP 0-50 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'SEP', 0, 5000000, 800, 900, false, 0.0830),
    (p_lnt_hl, 'SEP', 0, 5000000, 780, 799, false, 0.0850),
    (p_lnt_hl, 'SEP', 0, 5000000, 750, 779, false, 0.0850),
    (p_lnt_hl, 'SEP', 0, 5000000, 730, 749, false, 0.0865),
    (p_lnt_hl, 'SEP', 0, 5000000, 700, 729, false, 0.0865),
    (p_lnt_hl, 'SEP', 0, 5000000, 650, 700, false, 0.0920),
    (p_lnt_hl, 'SEP', 0, 5000000, null, null, true, 0.0865),
    (p_lnt_hl, 'SENP', 0, 5000000, 800, 900, false, 0.0830),
    (p_lnt_hl, 'SENP', 0, 5000000, 780, 799, false, 0.0850),
    (p_lnt_hl, 'SENP', 0, 5000000, 750, 779, false, 0.0850),
    (p_lnt_hl, 'SENP', 0, 5000000, 730, 749, false, 0.0865),
    (p_lnt_hl, 'SENP', 0, 5000000, 700, 729, false, 0.0865),
    (p_lnt_hl, 'SENP', 0, 5000000, 650, 700, false, 0.0920),
    (p_lnt_hl, 'SENP', 0, 5000000, null, null, true, 0.0865);

    -- SEP/ SENP >50-100 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'SEP', 5000001, 10000000, 800, 900, false, 0.0825),
    (p_lnt_hl, 'SEP', 5000001, 10000000, 780, 799, false, 0.0845),
    (p_lnt_hl, 'SEP', 5000001, 10000000, 750, 779, false, 0.0845),
    (p_lnt_hl, 'SEP', 5000001, 10000000, 730, 749, false, 0.0860),
    (p_lnt_hl, 'SEP', 5000001, 10000000, 700, 729, false, 0.0860),
    (p_lnt_hl, 'SEP', 5000001, 10000000, 650, 700, false, 0.0910),
    (p_lnt_hl, 'SEP', 5000001, 10000000, null, null, true, 0.0860),
    (p_lnt_hl, 'SENP', 5000001, 10000000, 800, 900, false, 0.0825),
    (p_lnt_hl, 'SENP', 5000001, 10000000, 780, 799, false, 0.0845),
    (p_lnt_hl, 'SENP', 5000001, 10000000, 750, 779, false, 0.0845),
    (p_lnt_hl, 'SENP', 5000001, 10000000, 730, 749, false, 0.0860),
    (p_lnt_hl, 'SENP', 5000001, 10000000, 700, 729, false, 0.0860),
    (p_lnt_hl, 'SENP', 5000001, 10000000, 650, 700, false, 0.0910),
    (p_lnt_hl, 'SENP', 5000001, 10000000, null, null, true, 0.0860);

    -- SEP/ SENP >100-150 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'SEP', 10000001, 15000000, 800, 900, false, 0.0825),
    (p_lnt_hl, 'SEP', 10000001, 15000000, 780, 799, false, 0.0830),
    (p_lnt_hl, 'SEP', 10000001, 15000000, 750, 779, false, 0.0830),
    (p_lnt_hl, 'SEP', 10000001, 15000000, 730, 749, false, 0.0845),
    (p_lnt_hl, 'SEP', 10000001, 15000000, 700, 729, false, 0.0845),
    (p_lnt_hl, 'SEP', 10000001, 15000000, 650, 700, false, 0.0895),
    (p_lnt_hl, 'SEP', 10000001, 15000000, null, null, true, 0.0845),
    (p_lnt_hl, 'SENP', 10000001, 15000000, 800, 900, false, 0.0825),
    (p_lnt_hl, 'SENP', 10000001, 15000000, 780, 799, false, 0.0830),
    (p_lnt_hl, 'SENP', 10000001, 15000000, 750, 779, false, 0.0830),
    (p_lnt_hl, 'SENP', 10000001, 15000000, 730, 749, false, 0.0845),
    (p_lnt_hl, 'SENP', 10000001, 15000000, 700, 729, false, 0.0845),
    (p_lnt_hl, 'SENP', 10000001, 15000000, 650, 700, false, 0.0895),
    (p_lnt_hl, 'SENP', 10000001, 15000000, null, null, true, 0.0845);

    -- SEP/ SENP >150 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'SEP', 15000001, 50000000, 800, 900, false, 0.0820),
    (p_lnt_hl, 'SEP', 15000001, 50000000, 780, 799, false, 0.0825),
    (p_lnt_hl, 'SEP', 15000001, 50000000, 750, 779, false, 0.0825),
    (p_lnt_hl, 'SEP', 15000001, 50000000, 730, 749, false, 0.0840),
    (p_lnt_hl, 'SEP', 15000001, 50000000, 700, 729, false, 0.0840),
    (p_lnt_hl, 'SEP', 15000001, 50000000, 650, 700, false, 0.0890),
    (p_lnt_hl, 'SEP', 15000001, 50000000, null, null, true, 0.0840),
    (p_lnt_hl, 'SENP', 15000001, 50000000, 800, 900, false, 0.0820),
    (p_lnt_hl, 'SENP', 15000001, 50000000, 780, 799, false, 0.0825),
    (p_lnt_hl, 'SENP', 15000001, 50000000, 750, 779, false, 0.0825),
    (p_lnt_hl, 'SENP', 15000001, 50000000, 730, 749, false, 0.0840),
    (p_lnt_hl, 'SENP', 15000001, 50000000, 700, 729, false, 0.0840),
    (p_lnt_hl, 'SENP', 15000001, 50000000, 650, 700, false, 0.0890),
    (p_lnt_hl, 'SENP', 15000001, 50000000, null, null, true, 0.0840);
END IF;

IF p_lnt_lap IS NOT NULL THEN
    -- Copy same structure for LAP by re-selecting the exact matrix
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi)
    SELECT p_lnt_lap, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi
    FROM product_roi_matrix WHERE product_id = p_lnt_hl;
END IF;

END $$;
