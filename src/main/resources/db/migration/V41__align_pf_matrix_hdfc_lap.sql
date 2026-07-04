-- ═══════════════════════════════════════════════════════════════════════════════
-- V41 — ALIGN HDFC LAP SELF-EMPLOYED PROCESSING FEE SLAB
-- ═══════════════════════════════════════════════════════════════════════════════

DO $$
DECLARE
    p_id BIGINT;
BEGIN

-- Correct HDFC-LAP-0002 (HDFC LAP Self Employed) processing fee slab min_loan_amount
SELECT id INTO p_id FROM loan_products WHERE product_code = 'HDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    -- Delete the old incorrect slab that started at 50,000,000
    DELETE FROM product_pf_matrix WHERE product_id = p_id;
    
    -- Insert the correct slab starting at 0.00 to No Limit as per the spreadsheet
    INSERT INTO product_pf_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, fee_value, is_flat, tax_rate) VALUES
    (p_id, 'SEP_SENP', 0.00, 999999999.00, 0.0050, false, 0.1800);
END IF;

END $$;
