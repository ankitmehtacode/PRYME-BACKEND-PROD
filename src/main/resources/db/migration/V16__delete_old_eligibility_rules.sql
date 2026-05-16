-- =============================================================================
-- V16: Delete Old Inactive Eligibility Rules
-- The previous V13 migration deactivated old rules for active products.
-- This migration hard-deletes those deactivated rules to clean up the matrix.
-- =============================================================================

DELETE FROM eligibility_conditions 
WHERE is_active = FALSE 
AND product_code IN ('LT_HL_001','SBI_HL_001','HDFC_HL_001','BAJAJ_LAP_001','TATA_LAP_001','ICICI_LAP_001','PNB_LAP_001');
