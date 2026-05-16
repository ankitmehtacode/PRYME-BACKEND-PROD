-- =============================================================================
-- V17: PURGE ALL MOCK ENGINE DATA & DROP COMPUTATION COLUMNS
-- =============================================================================
-- All existing engine policies, loan products, banks, ROI/PF computation
-- logic were mock/test data. This migration wipes the slate clean so
-- real production data can be seeded fresh.
--
-- ⚠️  DESTRUCTIVE: Deletes ALL rows from eligibility_conditions,
--     loan_products, and banks. Also drops 4 SpEL computation columns.
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- STEP 1: DELETE all eligibility conditions (active + inactive)
-- ─────────────────────────────────────────────────────────────────────────────
DELETE FROM eligibility_conditions;

-- ─────────────────────────────────────────────────────────────────────────────
-- STEP 2: DELETE all loan products
-- ─────────────────────────────────────────────────────────────────────────────
DELETE FROM loan_products;

-- ─────────────────────────────────────────────────────────────────────────────
-- STEP 3: DELETE all banks
-- ─────────────────────────────────────────────────────────────────────────────
DELETE FROM banks;

-- ─────────────────────────────────────────────────────────────────────────────
-- STEP 4: DROP SpEL computation logic columns (mock schemas)
-- ─────────────────────────────────────────────────────────────────────────────

-- ROI computation logic (added in V9, widened in V15)
ALTER TABLE loan_products DROP COLUMN IF EXISTS roi_computation_logic;

-- Processing Fee computation logic (added in V8)
ALTER TABLE loan_products DROP COLUMN IF EXISTS pf_computation_logic;

-- LTV computation logic (added in V12)
ALTER TABLE eligibility_conditions DROP COLUMN IF EXISTS ltv_computation_logic;

-- FOIR computation logic (added in V12)
ALTER TABLE eligibility_conditions DROP COLUMN IF EXISTS foir_computation_logic;


-- =============================================================================
-- POST-MIGRATION VERIFICATION (run manually):
--   SELECT COUNT(*) FROM eligibility_conditions;  -- 0
--   SELECT COUNT(*) FROM loan_products;            -- 0
--   SELECT COUNT(*) FROM banks;                    -- 0
--   SELECT column_name FROM information_schema.columns
--     WHERE table_name = 'loan_products'
--       AND column_name IN ('roi_computation_logic', 'pf_computation_logic');
--   -- Should return 0 rows
-- =============================================================================
