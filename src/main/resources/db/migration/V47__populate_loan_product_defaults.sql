-- ==============================================================================
-- V47: Populate Default FOIR and LTV for Loan Products
-- ==============================================================================
-- The backend engine relies on loan_products.ltv and loan_products.max_emi_nmi_ratio
-- as fallbacks when eligibility conditions don't specify them.
-- These were previously left NULL, causing effectiveLtv to evaluate to 0,
-- which blocked all cascade resolutions (LTV_EXCEEDED).
-- ==============================================================================

UPDATE loan_products 
SET ltv = 0.80 
WHERE ltv IS NULL;

UPDATE loan_products 
SET max_emi_nmi_ratio = 0.65 
WHERE max_emi_nmi_ratio IS NULL;
