-- =============================================================================
-- V5: Schema Patch + HDFC Test Seed Data
-- 1. Add missing columns to eligibility_conditions (entity ↔ DDL gap)
-- 2. Seed HDFC bank + 4 loan products + 4 eligibility conditions
-- =============================================================================

-- ─── SCHEMA PATCH: Missing columns from EligibilityCondition entity ─────────
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS bank_name           VARCHAR(255);
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS loan_type           VARCHAR(255);
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS itr_required_years  INTEGER;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS ltv_allowed         NUMERIC(5,4);
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS deviation_formulae  TEXT;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS conditions          TEXT;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS emi_not_obligated   BOOLEAN;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS negative_property   TEXT;

-- ─── SEED: HDFC Bank ────────────────────────────────────────────────────────
INSERT INTO banks (bank_name, logo_url, active)
VALUES ('HDFC Bank', '/logos/hdfc.svg', TRUE)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

-- =============================================================================
-- HDFC LOAN PRODUCTS (4 loan types)
-- ROI stored as decimal: 0.0875 = 8.75%, LTV as decimal: 0.75 = 75%
-- FOIR (max_emi_nmi_ratio) as decimal: 0.65 = 65%
-- =============================================================================

-- ── 1. HDFC HOME LOAN ───────────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'HDFC_HL_001', 'HDFC Home Loan', 'HOME_LOAN', 1, 'HDFC Bank', 'FLOATING',
    650, 900, 0.0875, 0.0050,
    12, 360, 500000.00, 50000000.00,
    0.6500, 0.7500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 2. HDFC BUSINESS LOAN ───────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'HDFC_BL_001', 'HDFC Business Loan', 'BUSINESS_LOAN', 1, 'HDFC Bank', 'FLOATING',
    700, 900, 0.1200, 0.0200,
    12, 84, 300000.00, 10000000.00,
    0.5500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 3. HDFC PERSONAL LOAN ───────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'HDFC_PL_001', 'HDFC Personal Loan', 'PERSONAL_LOAN', 1, 'HDFC Bank', 'FIXED',
    725, 900, 0.1050, 0.0200,
    12, 60, 100000.00, 4000000.00,
    0.5000, 1.0000, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 4. HDFC LAP (Loan Against Property) ─────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'HDFC_LAP_001', 'HDFC Loan Against Property', 'LAP', 1, 'HDFC Bank', 'FLOATING',
    675, 900, 0.0950, 0.0100,
    12, 180, 500000.00, 50000000.00,
    0.6000, 0.6000, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- =============================================================================
-- ELIGIBILITY CONDITIONS (linked to the products above by product_id)
-- These are the "Engine Rules" configured via AdminEligibilityModal
-- =============================================================================

-- We need the auto-generated product IDs. Use subqueries.

-- ── HOME LOAN Salaried Rule ────────────────────────────────────────────────
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max, ltv_allowed,
    property_type, itr_required_years, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    2, 0.6500, 0.7500,
    'RESIDENTIAL', 0, TRUE
);

-- ── BUSINESS LOAN Self-Employed Rule ───────────────────────────────────────
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, foir_max, ltv_allowed,
    itr_required_years, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_BL_001'),
    'HDFC_BL_001', 'HDFC Bank', 'BUSINESS_LOAN',
    'SELF_EMPLOYED', 25, 65, 30000.00, 'NIP',
    3, 0.5500, 0.6500,
    2, TRUE
);

-- ── PERSONAL LOAN Salaried Rule ────────────────────────────────────────────
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max,
    is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_PL_001'),
    'HDFC_PL_001', 'HDFC Bank', 'PERSONAL_LOAN',
    'SALARIED', 21, 58, 25000.00, 'STANDARD',
    1, 0.5000,
    TRUE
);

-- ── LAP Professional Rule ──────────────────────────────────────────────────
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max, ltv_allowed,
    property_type, itr_required_years, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_LAP_001'),
    'HDFC_LAP_001', 'HDFC Bank', 'LAP',
    'PROFESSIONAL', 25, 65, 40000.00, 'SENP',
    3, 0.6000, 0.6000,
    'RESIDENTIAL,COMMERCIAL', 2, TRUE
);
