-- ═══════════════════════════════════════════════════════════════════════════════
-- V40 — SEED EXACT LOGIN FEES FOR YES BANK, HDFC LAP, AND IDFC LAP
-- ═══════════════════════════════════════════════════════════════════════════════

DO $$
DECLARE
    p_id BIGINT;
BEGIN

-- YES-HL-0001 (YES HL Salaried - 0.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0001';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 0.00, 999999999.00, 0.00);
END IF;

-- YES-HL-0002 (YES HL Self Employed - 0.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0002';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 0.00, 999999999.00, 0.00);
END IF;

-- YES-LAP-0001 (YES LAP Salaried - 2340.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0001';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 0.00, 999999999.00, 2340.00);
END IF;

-- YES-LAP-0002 (YES LAP Self Employed - 2340.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 0.00, 999999999.00, 2340.00);
END IF;

-- HDFC-LAP-0001 (HDFC LAP Salaried - 5900.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SALARIED_SEP', 0.00, 999999999.00, 5900.00);
END IF;

-- HDFC-LAP-0002 (HDFC LAP Self Employed - 5900.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 0.00, 999999999.00, 5900.00);
END IF;

-- IDFC-LAP-0002 (IDFC LAP Self Employed - 5900.00)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    DELETE FROM product_login_fee_matrix WHERE product_id = p_id;
    INSERT INTO product_login_fee_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, login_fee) VALUES
    (p_id, 'SEP_SENP', 0.00, 999999999.00, 5900.00);
END IF;

END $$;
