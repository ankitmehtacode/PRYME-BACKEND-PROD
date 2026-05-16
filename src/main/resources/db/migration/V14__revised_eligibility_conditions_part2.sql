-- =============================================================================
-- V14: REVISED ELIGIBILITY CONDITIONS — PART 2
-- Bajaj Finserv, Tata Capital, ICICI Bank, PNB Housing (all LAP)
-- =============================================================================

-- Shared blacklist stored as a reusable text block:
-- Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;
-- Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;
-- Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;
-- Real estate brokers;Insurance agents;Travel agents;Freelance consultants;
-- Ticket resellers;Builders;Land aggregators;Construction contractors;
-- Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;
-- Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours


-- =============================================================================
-- BAJAJ FINSERV LAP (BAJAJ_LAP_001) — 4 Lanes
-- =============================================================================

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'NIP',
    'SALARIED', 21, 65, 20000.00, 'STANDARD',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot', NULL, TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'NIP',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'BANKING',
    2, 3, 0.6500, 0.7500,
    'ABB 5,10,20,25 of every month', 'Upto 4 Account of Applicant & Co Applicant (SBA & CA)',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'Last 12M GSTR 3B Turnover * Profit Margin', 'Profit Margin : Service - 10%, Retailer - 12%, Wholeseller - 8%, Manufacturer - 4%',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);


-- =============================================================================
-- TATA CAPITAL LAP (TATA_LAP_001) — 4 Lanes
-- =============================================================================

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'NIP',
    'SALARIED', 21, 65, 20000.00, 'STANDARD',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot', NULL, TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'NIP',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'BANKING',
    2, 3, 0.6500, 0.7500,
    'ABB 5,10,20,25 of every month', 'Upto 4 Account of Applicant & Co Applicant (SBA & CA)',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'Last 12M GSTR 3B Turnover * Profit Margin', 'Profit Margin : Service - 10%, Retailer - 12%, Wholeseller - 8%, Manufacturer - 4%',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);


-- =============================================================================
-- ICICI BANK LAP (ICICI_LAP_001) — 4 Lanes
-- =============================================================================

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'NIP',
    'SALARIED', 21, 65, 20000.00, 'STANDARD',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot', NULL, TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'NIP',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'BANKING',
    2, 3, 0.6500, 0.7500,
    'ABB 5,10,20,25 of every month', 'Upto 4 Account of Applicant & Co Applicant (SBA & CA)',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'Last 12M GSTR 3B Turnover * Profit Margin', 'Profit Margin : Service - 10%, Retailer - 12%, Wholeseller - 8%, Manufacturer - 4%',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);


-- =============================================================================
-- PNB HOUSING LAP (PNB_LAP_001) — 4 Lanes
-- =============================================================================

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'NIP',
    'SALARIED', 21, 65, 20000.00, 'STANDARD',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot', NULL, TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'NIP',
    2, 3, 0.6500, 0.7500,
    'PAT+Depreciation+Interest', 'Upto 2CR - CA Certified, Above 2CR - Audited',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'BANKING',
    2, 3, 0.6500, 0.7500,
    'ABB 5,10,20,25 of every month', 'Upto 4 Account of Applicant & Co Applicant (SBA & CA)',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);

INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    deviation_formulae, conditions,
    property_type, negative_property, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 65, 20000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'Last 12M GSTR 3B Turnover * Profit Margin', 'Profit Margin : Service - 10%, Retailer - 12%, Wholeseller - 8%, Manufacturer - 4%',
    'RESIDENTIAL', 'Plot',
    'Gambling businesses;Casino-related businesses;Lottery agents;Stock market traders;Crypto trading businesses;Scrap dealers;Recycling traders;Kirana stores;Street vendors;Hawkers;Commission agents;Pawn brokers;Money lenders;Real estate brokers;Insurance agents;Travel agents;Freelance consultants;Ticket resellers;Builders;Land aggregators;Construction contractors;Mining contractors;Sand suppliers;Liquor businesses;Tobacco businesses;Fireworks traders;Firearms dealers;Nightclubs;Production houses;Gaming parlours',
    TRUE
);
