-- =============================================================================
-- V6: HDFC Exhaustive Test Seed Data for Policy Matrix Engine
-- Adds heavily detailed eligibility parameters mimicking HDFC underwriting
-- across different brackets for LTV, FOIR, Age, and Geographies.
-- =============================================================================

-- =============================================================================
-- 1. HOME LOAN EXTENDED RULES
-- =============================================================================

-- Home Loan: Salaried High-Income Tier (Higher FOIR allowance)
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max, ltv_allowed,
    property_type, itr_required_years, emi_not_obligated, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN',
    'SALARIED', 21, 65, 100000.00, 'STANDARD',
    3, 0.7500, 0.8000,
    'RESIDENTIAL', 0, FALSE, TRUE
);

-- Home Loan: Self-Employed Professional (SEP)
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, foir_max, ltv_allowed,
    property_type, itr_required_years, emi_not_obligated, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN',
    'PROFESSIONAL', 25, 70, 50000.00, 'SEP',
    2, 0.6000, 0.7000,
    'RESIDENTIAL', 3, TRUE, TRUE
);


-- =============================================================================
-- 2. BUSINESS LOAN EXTENDED RULES
-- =============================================================================

-- Business Loan: Retail Merchants (High turnover, moderate margin)
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, foir_max, ltv_allowed,
    itr_required_years, deviation_formulae, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_BL_001'),
    'HDFC_BL_001', 'HDFC Bank', 'BUSINESS_LOAN',
    'SELF_EMPLOYED', 22, 60, 40000.00, 'ITR_BASED',
    5, 0.5000, 0.0000,
    3, 'Turnover * 0.15 for Max Eligible EMI', TRUE
);


-- =============================================================================
-- 3. PERSONAL LOAN EXTENDED RULES
-- =============================================================================

-- Personal Loan: CAT A Companies (MNCs / Large Corps)
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max, conditions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_PL_001'),
    'HDFC_PL_001', 'HDFC Bank', 'PERSONAL_LOAN',
    'SALARIED', 21, 60, 35000.00, 'CATEGORY_A',
    1, 0.6000, 'Company must be listed in HDFC CAT A Master List', TRUE
);

-- Personal Loan: CAT B / C Companies
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max, conditions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_PL_001'),
    'HDFC_PL_001', 'HDFC Bank', 'PERSONAL_LOAN',
    'SALARIED', 23, 58, 45000.00, 'CATEGORY_B',
    3, 0.4500, 'Company must be listed in HDFC CAT B/C Master List', TRUE
);


-- =============================================================================
-- 4. LAP (LOAN AGAINST PROPERTY) EXTENDED RULES
-- =============================================================================

-- LAP: SENP (Self Employed Non Professional) Commercial Property
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, foir_max, ltv_allowed,
    property_type, itr_required_years, negative_property, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_LAP_001'),
    'HDFC_LAP_001', 'HDFC Bank', 'LAP',
    'SELF_EMPLOYED', 28, 65, 80000.00, 'SENP',
    4, 0.6500, 0.5500,
    'COMMERCIAL', 3, 'Godowns, Industrial Sheds not allowed', TRUE
);

-- LAP: Salaried Residential Property (High LTV)
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, foir_max, ltv_allowed,
    property_type, itr_required_years, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_LAP_001'),
    'HDFC_LAP_001', 'HDFC Bank', 'LAP',
    'SALARIED', 25, 60, 60000.00, 'STANDARD',
    3, 0.6000, 0.7000,
    'RESIDENTIAL', 0, TRUE
);
