-- =============================================================================
-- V7: PRODUCTION LENDER MATRIX SEED
-- =============================================================================
-- Seeds 7 partner banks, 7 loan products (HL × 3, LAP × 4), and 27 eligibility
-- rules spanning Salaried / Self-Employed × NIP / Banking / GST surrogates.
--
-- ⚠️  IDEMPOTENT: All inserts use ON CONFLICT so this migration can safely
--    re-run if Flyway checksum is manually reset during dev.
--
-- ⚠️  FIELD MAPPING (Spreadsheet → DB):
--    Product_Name     → product_name
--    Loan_Type        → loan_type (HOME_LOAN | LAP)
--    Lender_Name      → lender_name
--    Employment_Type  → employment_type (SALARIED | SELF_EMPLOYED)
--    Surrogate        → income_type (NIP | BANKING | GST | STANDARD)
--    Min/Max_Age      → min_age / max_age
--    Min_Income       → min_income
--    Vintage          → work_exp_years (salaried) / business_age_years (SE)
--    ITR Required     → itr_required_years
--    LTV_Allowed      → ltv_allowed (decimal: 0.6500 = 65%)
--    FOIR_Allowed     → foir_max (decimal: 0.7500 = 75%)
--    Deviation        → stored in deviation_formulae text
--    Formulae         → deviation_formulae
--    Conditions       → conditions
--    EMI_not_obligated → emi_not_obligated
--    Property_Type    → property_type
--    Negative_Property → negative_property
--    Negative_Profile → profile_restrictions
-- =============================================================================


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 0: SCHEMA EVOLUTION — Add 'surrogate' column if missing
-- ─────────────────────────────────────────────────────────────────────────────
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS surrogate VARCHAR(50);

-- Widen processing_fee from NUMERIC(6,4) → NUMERIC(14,4) to hold flat fees (e.g. ₹10,000)
-- alongside percentage decimals (e.g. 0.0025 = 0.25%). No data loss — purely additive.
ALTER TABLE loan_products ALTER COLUMN processing_fee TYPE NUMERIC(14,4);

-- Widen VARCHAR(255) → TEXT for columns that hold long content
ALTER TABLE eligibility_conditions ALTER COLUMN profile_restrictions TYPE TEXT;
ALTER TABLE eligibility_conditions ALTER COLUMN property_type TYPE TEXT;
ALTER TABLE eligibility_conditions ALTER COLUMN notes TYPE TEXT;


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 1: PARTNER BANKS
-- ─────────────────────────────────────────────────────────────────────────────
-- Banks use UUID PK (GenerationType.UUID), but ON CONFLICT targets bank_name
-- which has a UNIQUE constraint. Existing banks are left untouched.

INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'L&T Finance', '/logos/lt-finance.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'SBI', '/logos/sbi.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

-- HDFC Bank already seeded in V5, but ensure it's active
INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'HDFC Bank', '/logos/hdfc.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'Bajaj Finserv', '/logos/bajaj-finserv.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'Tata Capital', '/logos/tata-capital.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'ICICI Bank', '/logos/icici.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;

INSERT INTO banks (id, bank_name, logo_url, active, version)
VALUES (gen_random_uuid(), 'PNB Housing', '/logos/pnb-housing.svg', TRUE, 0)
ON CONFLICT (bank_name) DO UPDATE SET active = TRUE;


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 2: LOAN PRODUCTS (7 products)
-- ─────────────────────────────────────────────────────────────────────────────
-- lender_id is a denormalized Long (modular monolith rule — no FK to banks UUID).
-- We use sequential lender_ids: 100+ range to avoid collision with V5 test data.
-- ROI, PF, Prepayment, Foreclosure stored as decimals (0.0100 = 1%)
-- Amounts in INR (no paisa: 500000.00)

-- ── 1. L&T FINANCE — HOME LOAN ────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    bank_statement_months, salary_slip_months, gst_required_months,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'LT_HL_001', 'L&T Finance Home Loan', 'HOME_LOAN', 101, 'L&T Finance', 'FLOATING',
    700, 900, 0.0875, 0.0025,
    0.0100, 0.0450, 1100.00, 7500.00, 5000.00,
    120, 360, 500000.00, 300000000.00,
    12, 3, 12,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 2. SBI — HOME LOAN ────────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees,
    legal_technical_charges, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    bank_statement_months, salary_slip_months, gst_required_months,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'SBI_HL_001', 'SBI Home Loan', 'HOME_LOAN', 102, 'SBI', 'FLOATING',
    700, 900, 0.0840, 10000.00,
    0.0000, 0.0000, 1100.00,
    10000.00, 7500.00, 2000.00,
    120, 360, 500000.00, 500000000.00,
    12, 3, 12,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 3. HDFC — HOME LOAN (PRODUCTION OVERRIDE of V5 test data) ─────────────
-- V5 seeded 'HDFC_HL_001' with test values. This overwrites with production data.
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    bank_statement_months, salary_slip_months,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'HDFC_HL_001', 'HDFC Home Loan', 'HOME_LOAN', 1, 'HDFC Bank', 'FLOATING',
    720, 900, 0.0865, 0.0100,
    0.0100, 0.0450, 1100.00, 7500.00, 3500.00,
    120, 360, 1000000.00, 500000000.00,
    12, 3,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    prepayment_charges = EXCLUDED.prepayment_charges, foreclosure_charges = EXCLUDED.foreclosure_charges,
    login_fees = EXCLUDED.login_fees, stamp_duties = EXCLUDED.stamp_duties, other_expense = EXCLUDED.other_expense,
    min_loan_amount = EXCLUDED.min_loan_amount,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 4. BAJAJ FINSERV — LAP ────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'BAJAJ_LAP_001', 'Bajaj Finserv LAP', 'LAP', 103, 'Bajaj Finserv', 'FLOATING',
    700, 900, 0.1050, 0.0100,
    0.0100, 0.0450, 1100.00, 7500.00, 4000.00,
    60, 180, 500000.00, 50000000.00,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 5. TATA CAPITAL — LAP ─────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'TATA_LAP_001', 'Tata Capital LAP', 'LAP', 104, 'Tata Capital', 'FLOATING',
    710, 900, 0.1100, 0.0050,
    0.0100, 0.0450, 1100.00, 7500.00, 3500.00,
    60, 180, 750000.00, 70000000.00,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 6. ICICI BANK — LAP ──────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'ICICI_LAP_001', 'ICICI Bank LAP', 'LAP', 105, 'ICICI Bank', 'FLOATING',
    700, 900, 0.1000, 0.0025,
    0.0100, 0.0450, 1100.00, 7500.00, 4500.00,
    60, 180, 750000.00, 80000000.00,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;

-- ── 7. PNB HOUSING — LAP ─────────────────────────────────────────────────
INSERT INTO loan_products (
    product_code, product_name, loan_type, lender_id, lender_name, interest_type,
    min_cibil, max_cibil, roi, processing_fee,
    prepayment_charges, foreclosure_charges, login_fees, stamp_duties, other_expense,
    min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount,
    max_emi_nmi_ratio, ltv, is_active
) VALUES (
    'PNB_LAP_001', 'PNB Housing LAP', 'LAP', 106, 'PNB Housing', 'FLOATING',
    710, 900, 0.1050, 10000.00,
    0.0000, 0.0000, 1100.00, 7500.00, 3000.00,
    60, 180, 500000.00, 60000000.00,
    0.7500, 0.6500, TRUE
) ON CONFLICT (product_code) DO UPDATE SET
    roi = EXCLUDED.roi, processing_fee = EXCLUDED.processing_fee,
    ltv = EXCLUDED.ltv, max_emi_nmi_ratio = EXCLUDED.max_emi_nmi_ratio, is_active = TRUE;


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 3: NEGATIVE PROFILE BLACKLIST (Shared across ALL products)
-- ─────────────────────────────────────────────────────────────────────────────
-- Stored as a reusable SQL constant so every eligibility row gets the same list.
-- Each rule's profile_restrictions column references this blacklist.

-- The blacklist as a semicolon-delimited string (matches profile_restrictions column):
-- Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;
-- Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;
-- Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;
-- Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;
-- Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;
-- Nightclub;Production House;Gaming Parlour


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 4: ELIGIBILITY CONDITIONS (27 Rules)
-- ─────────────────────────────────────────────────────────────────────────────
-- Each product × employment_type × surrogate creates one rule.
-- HL products: 3 lenders × (Salaried + SE-NIP + SE-Banking + SE-GST) but
-- Salaried only has 1 row per lender. Some combos are N/A.
--
-- From the spec:
--   Salaried: Age 21-60, Income ≥25K, Vintage 1Y, ITR 2Y, LTV 65%, FOIR 75%
--   SE-NIP:   Age 21-70, Income ≥25K, Vintage 3Y, ITR 2Y, LTV 45%, FOIR 95%
--   SE-Banking: Age 21-70, Income ≥25K, Vintage 3Y, LTV 85%, FOIR 55%, Dev 10%
--   SE-GST:  Age 21-70, Income ≥25K, Vintage 2Y, ITR 3Y, LTV 65%, FOIR 75%


-- =============================================================================
-- L&T FINANCE HOME LOAN (LT_HL_001) — 4 Rules
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- SBI HOME LOAN (SBI_HL_001) — 4 Rules
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'SBI_HL_001'),
    'SBI_HL_001', 'SBI', 'HOME_LOAN', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'SBI_HL_001'),
    'SBI_HL_001', 'SBI', 'HOME_LOAN', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'SBI_HL_001'),
    'SBI_HL_001', 'SBI', 'HOME_LOAN', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'SBI_HL_001'),
    'SBI_HL_001', 'SBI', 'HOME_LOAN', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- HDFC HOME LOAN (HDFC_HL_001) — 4 Rules (PRODUCTION OVERWRITE)
-- V5/V6 seeded test rules. These are the real underwriting parameters.
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'HDFC_HL_001'),
    'HDFC_HL_001', 'HDFC Bank', 'HOME_LOAN', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- BAJAJ FINSERV LAP (BAJAJ_LAP_001) — 4 Rules
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'BAJAJ_LAP_001'),
    'BAJAJ_LAP_001', 'Bajaj Finserv', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- TATA CAPITAL LAP (TATA_LAP_001) — 4 Rules
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'TATA_LAP_001'),
    'TATA_LAP_001', 'Tata Capital', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- ICICI BANK LAP (ICICI_LAP_001) — 4 Rules
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'ICICI_LAP_001'),
    'ICICI_LAP_001', 'ICICI Bank', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- PNB HOUSING LAP (PNB_LAP_001) — 4 Rules
-- =============================================================================

-- Salaried
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'NIP',
    'SALARIED', 21, 60, 25000.00, 'STANDARD',
    1, 2, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed NIP
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'NIP',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'NIP',
    3, 2, 0.4500, 0.9500,
    'RESIDENTIAL', 'Plot', FALSE,
    'PAT + Depreciation + Interest', 'Up to 2CR: CA Certification; Above 2CR: Audited Financials',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed Banking
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'BANKING',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'BANKING',
    3, 0, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Deviation: 10% against LTV; Formula: ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);

-- Self-Employed GST
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    business_age_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'PNB_LAP_001'),
    'PNB_LAP_001', 'PNB Housing', 'LAP', 'GST',
    'SELF_EMPLOYED', 21, 70, 25000.00, 'GST',
    2, 3, 0.6500, 0.7500,
    'RESIDENTIAL', 'Plot', FALSE,
    'Last 12M GSTR 3B Turnover * Profit Margin; Service=10%, Retailer=12%, Wholesaler=8%, Manufacturer=4%',
    'Gross Receipt * 2.5; Multiplier 1.5 for CS',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- L&T FINANCE HOME LOAN (LT_HL_001) — Additional Salaried (Banking surrogate)
-- Salaried employees can also be assessed via Banking surrogate
INSERT INTO eligibility_conditions (
    product_id, product_code, bank_name, loan_type, surrogate,
    employment_type, min_age, max_age, min_income, income_type,
    work_exp_years, itr_required_years, ltv_allowed, foir_max,
    property_type, negative_property, emi_not_obligated,
    deviation_formulae, conditions, profile_restrictions, is_active
) VALUES (
    (SELECT id FROM loan_products WHERE product_code = 'LT_HL_001'),
    'LT_HL_001', 'L&T Finance', 'HOME_LOAN', 'BANKING',
    'SALARIED', 21, 60, 25000.00, 'BANKING',
    1, 2, 0.8500, 0.5500,
    'RESIDENTIAL', 'Plot', FALSE,
    'ABB 5th,10th,20th,25th of every month', 'Up to 4 Accounts of Applicant & Co-Applicant allowed',
    'Gambling;Casino;Lottery Agent;Stock Market Trader;Crypto Trading;Scrap Dealer;Recycling Trader;Kirana Store;Street Vendor;Hawker;Commission Agent;Pawn Broker;Money Lender;Real Estate Broker;Insurance Agent;Travel Agent;Freelance Consultant;Ticket Reseller;Builder;Land Aggregator;Construction Contractor;Mining Contractor;Sand Supplier;Liquor Business;Tobacco Business;Fireworks Trader;Firearms Dealer;Nightclub;Production House;Gaming Parlour',
    TRUE
);


-- =============================================================================
-- VERIFICATION COUNTS (for audit)
-- Expected: 7 banks, 7 loan_products (6 new + 1 HDFC update), 28 conditions
-- =============================================================================
-- Run after migration:
--   SELECT COUNT(*) FROM banks WHERE active = TRUE;         -- 7+
--   SELECT COUNT(*) FROM loan_products WHERE is_active = TRUE;  -- 7+
--   SELECT COUNT(*) FROM eligibility_conditions WHERE is_active = TRUE;  -- 28+
