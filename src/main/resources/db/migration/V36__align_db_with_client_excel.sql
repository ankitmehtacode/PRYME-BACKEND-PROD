-- ═══════════════════════════════════════════════════════════════════════════════
-- V36 — ALIGN DATABASE WITH CLIENT EXCEL SHEETS (Source of Truth)
-- ═══════════════════════════════════════════════════════════════════════════════
-- Generated: 2026-06-17 14:41:20
-- Source: eligibility workbook (1).xlsx, Login_fees (1).xlsx
-- Strategy: UPDATE existing rows to match client-specified policy limits.
--
-- WHAT THIS FIXES:
--   1. loan_products: min_cibil, min/max_loan_amount, min/max_tenure_months
--      were seeded with uniform defaults (650, 100K, 999M, 12, 360) in V27.
--      Client Excel specifies lender-specific values.
--   2. eligibility_conditions: min_age, max_age, min_income were seeded with
--      defaults (21, 65, 25000) in V27. Client Excel specifies per-product values.
--
-- WHAT THIS DOES NOT TOUCH:
--   - product_login_fee_matrix (V31 already has correct dynamic login fees)
--   - product_pf_matrix (V29 already has correct PF data)
--   - product_roi_matrix (V27 already has correct ROI tiers)
--   - Enrichment columns from V32 (negative lists, formulae, etc.)
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────────────────
-- PART 1: LOAN PRODUCT LIMITS — per product_code
-- ─────────────────────────────────────────────────────────────────────────────

-- ═══ ABFL ═══
UPDATE loan_products SET max_loan_amount = 75000000, max_tenure_months = 300, min_cibil = 675, min_loan_amount = 3500000, min_tenure_months = 36 WHERE product_code = 'ABFL-HL-0001';
UPDATE loan_products SET max_loan_amount = 75000000, max_tenure_months = 240, min_cibil = 675, min_loan_amount = 3500000, min_tenure_months = 36 WHERE product_code = 'ABFL-HL-0002';
UPDATE loan_products SET max_loan_amount = 75000000, max_tenure_months = 180, min_cibil = 675, min_loan_amount = 3500000, min_tenure_months = 36 WHERE product_code = 'ABFL-LAP-0001';
UPDATE loan_products SET max_loan_amount = 75000000, max_tenure_months = 180, min_cibil = 675, min_loan_amount = 3500000, min_tenure_months = 36 WHERE product_code = 'ABFL-LAP-0002';

-- ═══ BAJAJ ═══
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 384, min_cibil = 680, min_loan_amount = 3500000, min_tenure_months = 120 WHERE product_code = 'BAJAJ-HL-0001';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 384, min_cibil = 680, min_loan_amount = 3500000, min_tenure_months = 120 WHERE product_code = 'BAJAJ-HL-0002';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 384, min_cibil = 680, min_loan_amount = 3500000, min_tenure_months = 120 WHERE product_code = 'BAJAJ-HL-0003';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 240, min_cibil = 680, min_loan_amount = 3500000, min_tenure_months = 120 WHERE product_code = 'BAJAJ-LAP-0001';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 240, min_cibil = 680, min_loan_amount = 3500000, min_tenure_months = 120 WHERE product_code = 'BAJAJ-LAP-0002';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 240, min_cibil = 680, min_loan_amount = 3500000, min_tenure_months = 120 WHERE product_code = 'BAJAJ-LAP-0003';

-- ═══ BANDHAN ═══
UPDATE loan_products SET max_loan_amount = 80000000, max_tenure_months = 300, min_cibil = 700, min_loan_amount = 200000, min_tenure_months = 60 WHERE product_code = 'BANDHAN-HL-0001';
UPDATE loan_products SET max_loan_amount = 80000000, max_tenure_months = 300, min_cibil = 700, min_loan_amount = 200000, min_tenure_months = 60 WHERE product_code = 'BANDHAN-HL-0002';
UPDATE loan_products SET max_loan_amount = 80000000, max_tenure_months = 180, min_cibil = 700, min_loan_amount = 200000, min_tenure_months = 60 WHERE product_code = 'BANDHAN-LAP-0001';
UPDATE loan_products SET max_loan_amount = 80000000, max_tenure_months = 180, min_cibil = 700, min_loan_amount = 200000, min_tenure_months = 60 WHERE product_code = 'BANDHAN-LAP-0002';

-- ═══ BOB ═══
UPDATE loan_products SET min_loan_amount = 500000, min_tenure_months = 36 WHERE product_code = 'BOB-HL-0001';
UPDATE loan_products SET min_loan_amount = 500000, min_tenure_months = 36 WHERE product_code = 'BOB-HL-0002';
UPDATE loan_products SET max_tenure_months = 144, min_loan_amount = 500000, min_tenure_months = 36 WHERE product_code = 'BOB-LAP-0001';
UPDATE loan_products SET max_tenure_months = 144, min_loan_amount = 500000, min_tenure_months = 36 WHERE product_code = 'BOB-LAP-0002';

-- ═══ HDFC ═══
UPDATE loan_products SET max_loan_amount = 150000000, min_loan_amount = 2100000, min_tenure_months = 60 WHERE product_code = 'HDFC-HL-0001';
UPDATE loan_products SET max_loan_amount = 150000000, max_tenure_months = 300, min_loan_amount = 2100000, min_tenure_months = 60 WHERE product_code = 'HDFC-HL-0002';
UPDATE loan_products SET max_tenure_months = 180, min_loan_amount = 1100000, min_tenure_months = 60 WHERE product_code = 'HDFC-LAP-0001';
UPDATE loan_products SET max_tenure_months = 180, min_loan_amount = 1100000, min_tenure_months = 60 WHERE product_code = 'HDFC-LAP-0002';

-- ═══ ICICI ═══
UPDATE loan_products SET max_tenure_months = 240, min_cibil = 700, min_loan_amount = 2000000, min_tenure_months = 60 WHERE product_code = 'ICICI-HL-0001';
UPDATE loan_products SET max_tenure_months = 240, min_cibil = 700, min_loan_amount = 2000000, min_tenure_months = 60 WHERE product_code = 'ICICI-HL-0002';
UPDATE loan_products SET max_tenure_months = 180, min_cibil = 700, min_loan_amount = 2000000, min_tenure_months = 60 WHERE product_code = 'ICICI-LAP-0001';
UPDATE loan_products SET max_tenure_months = 180, min_cibil = 700, min_loan_amount = 2000000, min_tenure_months = 60 WHERE product_code = 'ICICI-LAP-0002';

-- ═══ IDBI ═══
UPDATE loan_products SET min_cibil = 700, min_loan_amount = 5000000, min_tenure_months = 60 WHERE product_code = 'IDBI-HL-0001';
UPDATE loan_products SET max_tenure_months = 300, min_cibil = 700, min_loan_amount = 5000000, min_tenure_months = 60 WHERE product_code = 'IDBI-HL-0002';
UPDATE loan_products SET max_tenure_months = 240, min_cibil = 700, min_loan_amount = 5000000, min_tenure_months = 60 WHERE product_code = 'IDBI-LAP-0001';
UPDATE loan_products SET max_tenure_months = 180, min_cibil = 700, min_loan_amount = 5000000, min_tenure_months = 60 WHERE product_code = 'IDBI-LAP-0002';

-- ═══ IDFC ═══
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 300, min_cibil = 600, min_loan_amount = 5000000, min_tenure_months = 60 WHERE product_code = 'IDFC-LAP-0001';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 300, min_cibil = 600, min_loan_amount = 5000000, min_tenure_months = 60 WHERE product_code = 'IDFC-LAP-0002';

-- ═══ JIO ═══
UPDATE loan_products SET max_loan_amount = 500000000, max_tenure_months = 300, min_loan_amount = 3000000, min_tenure_months = 60 WHERE product_code = 'JIO-HL-0001';
UPDATE loan_products SET max_loan_amount = 500000000, max_tenure_months = 240, min_loan_amount = 3000000, min_tenure_months = 60 WHERE product_code = 'JIO-HL-0002';
UPDATE loan_products SET max_loan_amount = 500000000, max_tenure_months = 180, min_loan_amount = 3000000, min_tenure_months = 60 WHERE product_code = 'JIO-LAP-0001';
UPDATE loan_products SET max_loan_amount = 500000000, max_tenure_months = 180, min_loan_amount = 3000000, min_tenure_months = 60 WHERE product_code = 'JIO-LAP-0002';

-- ═══ LT ═══
UPDATE loan_products SET max_loan_amount = 50000000, min_loan_amount = 2000000, min_tenure_months = 36 WHERE product_code = 'LT-HL-0001';
UPDATE loan_products SET max_loan_amount = 50000000, max_tenure_months = 240, min_loan_amount = 2000000, min_tenure_months = 36 WHERE product_code = 'LT-HL-0002';
UPDATE loan_products SET max_loan_amount = 50000000, max_tenure_months = 240, min_loan_amount = 2000000, min_tenure_months = 36 WHERE product_code = 'LT-LAP-0001';
UPDATE loan_products SET max_loan_amount = 50000000, max_tenure_months = 240, min_loan_amount = 2000000, min_tenure_months = 36 WHERE product_code = 'LT-LAP-0002';

-- ═══ SBI ═══
UPDATE loan_products SET min_cibil = 550, min_loan_amount = 1000000, min_tenure_months = 24 WHERE product_code = 'SBI-HL-0001';
UPDATE loan_products SET min_cibil = 550, min_loan_amount = 1000000, min_tenure_months = 24 WHERE product_code = 'SBI-HL-0002';
UPDATE loan_products SET max_tenure_months = 180, min_cibil = 600, min_loan_amount = 1000000, min_tenure_months = 60 WHERE product_code = 'SBI-LAP-0001';
UPDATE loan_products SET max_tenure_months = 180, min_cibil = 600, min_loan_amount = 1000000, min_tenure_months = 60 WHERE product_code = 'SBI-LAP-0002';

-- ═══ TATA ═══
UPDATE loan_products SET max_loan_amount = 100000000, min_loan_amount = 1000000, min_tenure_months = 60 WHERE product_code = 'TATA-HL-0001';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 240, min_loan_amount = 1000000, min_tenure_months = 60 WHERE product_code = 'TATA-HL-0002';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 180, min_loan_amount = 1000000, min_tenure_months = 60 WHERE product_code = 'TATA-LAP-0001';
UPDATE loan_products SET max_loan_amount = 100000000, max_tenure_months = 180, min_loan_amount = 1000000, min_tenure_months = 60 WHERE product_code = 'TATA-LAP-0002';

-- ═══ YES ═══
UPDATE loan_products SET max_loan_amount = 150000000, max_tenure_months = 180, min_cibil = 680, min_loan_amount = 2100000, min_tenure_months = 60 WHERE product_code = 'YES-HL-0001';
UPDATE loan_products SET max_loan_amount = 150000000, max_tenure_months = 180, min_cibil = 680, min_loan_amount = 2100000, min_tenure_months = 60 WHERE product_code = 'YES-HL-0002';
UPDATE loan_products SET max_loan_amount = 150000000, max_tenure_months = 180, min_cibil = 680, min_loan_amount = 2100000, min_tenure_months = 60 WHERE product_code = 'YES-LAP-0001';
UPDATE loan_products SET max_loan_amount = 150000000, max_tenure_months = 180, min_cibil = 680, min_loan_amount = 2100000, min_tenure_months = 60 WHERE product_code = 'YES-LAP-0002';

-- ─────────────────────────────────────────────────────────────────────────────
-- PART 2: ELIGIBILITY CONDITIONS — min_age, max_age, min_income
-- ─────────────────────────────────────────────────────────────────────────────
-- V27 seeded defaults: min_age=21, max_age=65, min_income=25000 for all.
-- Client Excel specifies lender-specific values per employment type × surrogate.

-- ABFL-HL-0001
UPDATE eligibility_conditions SET min_age = 22, max_age = 62, min_income = 30000 WHERE product_code = 'ABFL-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 62, min_income = 30000 WHERE product_code = 'ABFL-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- ABFL-HL-0002
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- ABFL-LAP-0001
UPDATE eligibility_conditions SET min_age = 22, max_age = 62, min_income = 30000 WHERE product_code = 'ABFL-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 62, min_income = 30000 WHERE product_code = 'ABFL-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- ABFL-LAP-0002
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 22, max_age = 80, min_income = 30000 WHERE product_code = 'ABFL-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BAJAJ-HL-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- BAJAJ-HL-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BAJAJ-HL-0003
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-HL-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BAJAJ-LAP-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- BAJAJ-LAP-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BAJAJ-LAP-0003
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 30000 WHERE product_code = 'BAJAJ-LAP-0003' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BANDHAN-HL-0001
UPDATE eligibility_conditions SET max_age = 60, min_income = 15000 WHERE product_code = 'BANDHAN-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- BANDHAN-HL-0002
UPDATE eligibility_conditions SET max_age = 75, min_income = 15000 WHERE product_code = 'BANDHAN-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET max_age = 75, min_income = 15000 WHERE product_code = 'BANDHAN-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET max_age = 75, min_income = 15000 WHERE product_code = 'BANDHAN-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BANDHAN-LAP-0001
UPDATE eligibility_conditions SET max_age = 60, min_income = 15000 WHERE product_code = 'BANDHAN-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- BANDHAN-LAP-0002
UPDATE eligibility_conditions SET max_age = 75, min_income = 15000 WHERE product_code = 'BANDHAN-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET max_age = 75, min_income = 15000 WHERE product_code = 'BANDHAN-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET max_age = 75, min_income = 15000 WHERE product_code = 'BANDHAN-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- BOB-HL-0001
UPDATE eligibility_conditions SET max_age = 60, min_income = 10000 WHERE product_code = 'BOB-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- BOB-HL-0002
UPDATE eligibility_conditions SET max_age = 70, min_income = 10000 WHERE product_code = 'BOB-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- BOB-LAP-0001
UPDATE eligibility_conditions SET max_age = 60, min_income = 10000 WHERE product_code = 'BOB-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- BOB-LAP-0002
UPDATE eligibility_conditions SET min_income = 10000 WHERE product_code = 'BOB-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- HDFC-HL-0001
UPDATE eligibility_conditions SET min_income = 40000 WHERE product_code = 'HDFC-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- HDFC-HL-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'HDFC-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'HDFC-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- HDFC-LAP-0001
UPDATE eligibility_conditions SET min_age = 20, max_age = 62, min_income = 30000 WHERE product_code = 'HDFC-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- HDFC-LAP-0002
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'HDFC-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'HDFC-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'HDFC-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- ICICI-HL-0001
UPDATE eligibility_conditions SET min_age = 20, max_age = 62, min_income = 30000 WHERE product_code = 'ICICI-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- ICICI-HL-0002
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'ICICI-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'ICICI-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'ICICI-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- ICICI-LAP-0001
UPDATE eligibility_conditions SET min_age = 20, max_age = 62, min_income = 30000 WHERE product_code = 'ICICI-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- ICICI-LAP-0002
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'ICICI-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'ICICI-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 20, max_age = 70, min_income = 30000 WHERE product_code = 'ICICI-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- IDBI-HL-0001
UPDATE eligibility_conditions SET min_age = 22, min_income = 40000 WHERE product_code = 'IDBI-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- IDBI-HL-0002
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'IDBI-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- IDBI-LAP-0001
UPDATE eligibility_conditions SET min_age = 22, min_income = 40000 WHERE product_code = 'IDBI-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- IDBI-LAP-0002
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'IDBI-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- IDFC-LAP-0001
UPDATE eligibility_conditions SET min_age = 22, max_age = 62 WHERE product_code = 'IDFC-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 62 WHERE product_code = 'IDFC-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- IDFC-LAP-0002
UPDATE eligibility_conditions SET min_age = 22, max_age = 75 WHERE product_code = 'IDFC-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 75 WHERE product_code = 'IDFC-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 22, max_age = 75 WHERE product_code = 'IDFC-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- JIO-HL-0001
UPDATE eligibility_conditions SET min_age = 22, max_age = 62, min_income = 40000 WHERE product_code = 'JIO-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 62, min_income = 40000 WHERE product_code = 'JIO-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- JIO-HL-0002
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'JIO-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'JIO-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'JIO-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'JIO-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 22, max_age = 75, min_income = 40000 WHERE product_code = 'JIO-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- JIO-LAP-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 60 WHERE product_code = 'JIO-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 60 WHERE product_code = 'JIO-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- JIO-LAP-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 75 WHERE product_code = 'JIO-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 75 WHERE product_code = 'JIO-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75 WHERE product_code = 'JIO-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75 WHERE product_code = 'JIO-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75 WHERE product_code = 'JIO-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- LT-HL-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 60, min_income = 30000 WHERE product_code = 'LT-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- LT-HL-0002
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- LT-LAP-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 60, min_income = 30000 WHERE product_code = 'LT-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- LT-LAP-0002
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 25, max_age = 70 WHERE product_code = 'LT-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- SBI-HL-0001
UPDATE eligibility_conditions SET min_age = 20 WHERE product_code = 'SBI-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- SBI-HL-0002
UPDATE eligibility_conditions SET min_age = 20, max_age = 70 WHERE product_code = 'SBI-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- SBI-LAP-0001
UPDATE eligibility_conditions SET min_age = 20 WHERE product_code = 'SBI-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- SBI-LAP-0002
UPDATE eligibility_conditions SET min_age = 20, max_age = 70 WHERE product_code = 'SBI-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- TATA-HL-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 40000 WHERE product_code = 'TATA-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 40000 WHERE product_code = 'TATA-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- TATA-HL-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 40000 WHERE product_code = 'TATA-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 40000 WHERE product_code = 'TATA-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 40000 WHERE product_code = 'TATA-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';

-- TATA-LAP-0001
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 40000 WHERE product_code = 'TATA-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 62, min_income = 40000 WHERE product_code = 'TATA-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- TATA-LAP-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 40000 WHERE product_code = 'TATA-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 40000 WHERE product_code = 'TATA-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 70, min_income = 40000 WHERE product_code = 'TATA-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';

-- YES-HL-0001
UPDATE eligibility_conditions SET min_age = 23, min_income = 40000 WHERE product_code = 'YES-HL-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);

-- YES-HL-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'CPM SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-HL-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';

-- YES-LAP-0001
UPDATE eligibility_conditions SET min_age = 23, min_income = 40000 WHERE product_code = 'YES-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, min_income = 40000 WHERE product_code = 'YES-LAP-0001' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND surrogate = 'LOW LTV';

-- YES-LAP-0002
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND (surrogate = 'NIP' OR surrogate IS NULL);
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'LOW LTV';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'CPM SEP';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'BANKING';
UPDATE eligibility_conditions SET min_age = 23, max_age = 75, min_income = 40000 WHERE product_code = 'YES-LAP-0002' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate = 'GST';
