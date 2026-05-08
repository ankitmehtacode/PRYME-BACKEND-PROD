-- =============================================================================
-- V11: Deactivate V5/V6 Test Products
-- Only 7 production products should be active (from V7 seed):
--   HOME_LOAN:  LT_HL_001, SBI_HL_001, HDFC_HL_001
--   LAP:        BAJAJ_LAP_001, TATA_LAP_001, ICICI_LAP_001, PNB_LAP_001
--
-- V5/V6 seeded test products (HDFC_BL_001, HDFC_PL_001, HDFC_LAP_001)
-- are deactivated here along with their eligibility conditions.
-- =============================================================================

-- 1. Deactivate test loan products
UPDATE loan_products SET is_active = FALSE
WHERE product_code IN ('HDFC_BL_001', 'HDFC_PL_001', 'HDFC_LAP_001');

-- 2. Deactivate eligibility conditions tied to test products
UPDATE eligibility_conditions SET is_active = FALSE
WHERE product_code IN ('HDFC_BL_001', 'HDFC_PL_001', 'HDFC_LAP_001');

-- 3. Also deactivate any V5 duplicate conditions for HDFC_HL_001 that lack
--    the surrogate field (V7 re-seeded proper conditions with surrogates).
--    We identify stale V5/V6 conditions by: surrogate IS NULL AND product_code = 'HDFC_HL_001'
UPDATE eligibility_conditions SET is_active = FALSE
WHERE product_code = 'HDFC_HL_001'
  AND surrogate IS NULL;
