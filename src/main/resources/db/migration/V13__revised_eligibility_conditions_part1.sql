-- =============================================================================
-- V13: REVISED ELIGIBILITY CONDITIONS — PART 1
-- Corrects all eligibility conditions to match verified spreadsheet.
-- Part 1: Deactivate old, update products, seed L&T + SBI + HDFC conditions.
-- =============================================================================

-- ── STEP 1: Deactivate ALL existing eligibility conditions for active products
UPDATE eligibility_conditions SET is_active = FALSE
WHERE product_code IN ('LT_HL_001','SBI_HL_001','HDFC_HL_001','BAJAJ_LAP_001','TATA_LAP_001','ICICI_LAP_001','PNB_LAP_001');

-- ── STEP 2: Update loan_products with corrected values
UPDATE loan_products SET max_loan_amount = 30000000.00 WHERE product_code = 'LT_HL_001';
UPDATE loan_products SET max_loan_amount = 999999999.00 WHERE product_code = 'SBI_HL_001';
UPDATE loan_products SET max_loan_amount = 999999999.00 WHERE product_code = 'HDFC_HL_001';
UPDATE loan_products SET max_loan_amount = 5000000.00 WHERE product_code = 'BAJAJ_LAP_001';
UPDATE loan_products SET max_loan_amount = 7000000.00 WHERE product_code = 'TATA_LAP_001';
UPDATE loan_products SET max_loan_amount = 8000000.00 WHERE product_code = 'ICICI_LAP_001';
UPDATE loan_products SET max_loan_amount = 6000000.00 WHERE product_code = 'PNB_LAP_001';

-- ── STEP 3: L&T FINANCE HOME LOAN — 5 Lanes ────────────────────────────────

-- L&T Salaried NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot', NULL, TRUE
);

-- L&T Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- L&T Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions, emi_not_obligated,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 2, 0.8500, 0.5500,
    '10% against LTV; ABB 5,10,20,25 of every month', 'Upto 4 Account of Applicant & Co Applicant (SBA & CA)', FALSE,
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- L&T Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    3, 2, 0.7500, 0.6500,
    'Last 12M GSTR 3B Turnover * Profit Margin', 'Profit Margin : Service - 10%, Retailer - 12%, Wholeseller - 8%, Manufacturer - 4%',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- L&T SENP (Self-Employed Non-Professional) — NEW LANE
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'SENP',
    'SENP', 21, 70, 25000.00, 'SENP',
    2, 2, 0.6500, 0.7500,
    'Gross Receipt * 2.5', 'Multiplier of 1.5 for CS',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- ── STEP 4: SBI HOME LOAN — 2 Lanes (NIP only) ─────────────────────────────

-- SBI Salaried NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'SBI_HL_001'),
    'SBI_HL_001', 'SBI', 'HOME_LOAN', 'NIP',
    'SALARIED', 23, 65, 30000.00, 'STANDARD',
    2, 2, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', NULL, NULL, TRUE
);

-- SBI Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'SBI_HL_001'),
    'SBI_HL_001', 'SBI', 'HOME_LOAN', 'NIP',
    'SELF_EMPLOYED', 23, 65, 30000.00, 'NIP',
    2, 2, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', NULL,
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- ── STEP 5: HDFC HOME LOAN — 4 Lanes ────────────────────────────────────────

-- HDFC Salaried NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'NIP',
    'SALARIED', 21, 65, 20000.00, 'STANDARD',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot', NULL, TRUE
);

-- HDFC Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'NIP',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'NIP',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- HDFC Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'BANKING',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'BANKING',
    2, 3, 0.6500, 0.7500,
    'ABB 5,10,20,25 of every month', 'Upto 4 Account of Applicant & Co Applicant (SBA & CA)',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

-- HDFC Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'GST',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'Last 12M GSTR 3B Turnover * Profit Margin', 'Profit Margin : Service - 10%, Retailer - 12%, Wholeseller - 8%, Manufacturer - 4%',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);
