-- ==============================================================================
-- V19: Production Data Seed (Loan Products, ROI Matrix, Eligibility Conditions)
-- ==============================================================================
-- This migration seeds the exact production matrix provided by the business,
-- populating the banks, loan_products, product_roi_matrix, and eligibility_conditions.
-- ==============================================================================

-- ==========================================
-- 1. BANKS
-- ==========================================
INSERT INTO banks (bank_name, active) VALUES
('L&T Finance', true),
('ICICI Bank', true),
('Bandhan Bank', true),
('Aditya Birla Finance Limited', true),
('Bank of Baroda', true),
('SBI', true),
('Bajaj Finance', true),
('ICICI HFC', true),
('HDFC Bank', true);

-- ==========================================
-- 2. LOAN PRODUCTS
-- ==========================================
-- Using DO block to safely capture generated product IDs for dependent inserts

DO $$
DECLARE
    p_lnt_hl BIGINT;
    p_lnt_lap BIGINT;
    p_icici_hl BIGINT;
    p_icici_lap BIGINT;
    p_bajaj_aff_hl BIGINT;
    p_bajaj_aff_lap BIGINT;
    p_bajaj_np_hl BIGINT;
    p_bajaj_np_lap BIGINT;
    p_bajaj_prime_hl_sal BIGINT;
    p_bajaj_prime_hl_sep BIGINT;
    p_bajaj_prime_lap_sal BIGINT;
    p_bajaj_prime_lap_sep BIGINT;
    p_bandhan_hl BIGINT;
    p_bandhan_lap BIGINT;
    p_abfl_hl BIGINT;
    p_abfl_lap BIGINT;
    p_bob_hl BIGINT;
    p_bob_lap BIGINT;
    p_sbi_hl BIGINT;
    p_sbi_lap BIGINT;
    p_icicihfc_lap BIGINT;
    p_hdfc_hl BIGINT;
BEGIN

    -- 2.1 L&T Finance
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('LNT-HL-001', 'L&T Home Loan', 100, 'L&T Finance', 'HL', 'Floating', 650, 900, 0.085, 1000, 36, 360, 2000000, 50000000) RETURNING id INTO p_lnt_hl;
    
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('LNT-LAP-001', 'L&T LAP', 100, 'L&T Finance', 'LAP', 'Floating', 650, 900, 0.090, 1000, 36, 240, 2000000, 50000000) RETURNING id INTO p_lnt_lap;

    -- 2.2 ICICI Bank
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('ICICI-HL-001', 'ICICI Home Loan', 200, 'ICICI Bank', 'HL', 'Floating', 700, 900, 0.085, 3000, 60, 240, 2000000, 9999999999) RETURNING id INTO p_icici_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('ICICI-LAP-001', 'ICICI LAP', 200, 'ICICI Bank', 'LAP', 'Floating', 700, 900, 0.090, 5000, 60, 180, 2000000, 9999999999) RETURNING id INTO p_icici_lap;

    -- 2.3 Bajaj Affordable
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-AFF-HL', 'Bajaj Affordable HL', 300, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.090, 1770, 120, 384, 1500000, 10000000) RETURNING id INTO p_bajaj_aff_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-AFF-LAP', 'Bajaj Affordable LAP', 300, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.095, 3540, 120, 240, 1500000, 10000000) RETURNING id INTO p_bajaj_aff_lap;

    -- 2.4 Bajaj Near Prime
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-NP-HL', 'Bajaj Near Prime HL', 300, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.0925, 2360, 120, 384, 1500000, 30000000) RETURNING id INTO p_bajaj_np_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-NP-LAP', 'Bajaj Near Prime LAP', 300, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.0975, 3540, 120, 240, 1500000, 30000000) RETURNING id INTO p_bajaj_np_lap;

    -- 2.5 Bandhan Bank
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BANDHAN-HL', 'Bandhan Home Loan', 400, 'Bandhan Bank', 'HL', 'Floating', 700, 900, 0.085, 2360, 60, 300, 200000, 80000000) RETURNING id INTO p_bandhan_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BANDHAN-LAP', 'Bandhan LAP', 400, 'Bandhan Bank', 'LAP', 'Floating', 700, 900, 0.090, 2360, 60, 180, 200000, 80000000) RETURNING id INTO p_bandhan_lap;

    -- 2.6 Aditya Birla Finance Limited
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('ABFL-HL-001', 'ABFL Home Loan', 500, 'Aditya Birla Finance Limited', 'HL', 'Floating', 650, 900, 0.085, 2950, 60, 300, 3500000, 75000000) RETURNING id INTO p_abfl_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('ABFL-LAP-001', 'ABFL LAP', 500, 'Aditya Birla Finance Limited', 'LAP', 'Floating', 650, 900, 0.090, 5900, 60, 180, 3500000, 75000000) RETURNING id INTO p_abfl_lap;

    -- 2.7 Bank of Baroda
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BOB-HL-001', 'Bank of Baroda Home Loan', 600, 'Bank of Baroda', 'HL', 'Floating', 650, 900, 0.085, 0, 60, 300, 500000, 9999999999) RETURNING id INTO p_bob_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BOB-LAP-001', 'Bank of Baroda LAP', 600, 'Bank of Baroda', 'LAP', 'Floating', 650, 900, 0.090, 0, 60, 180, 500000, 9999999999) RETURNING id INTO p_bob_lap;

    -- 2.8 SBI
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('SBI-HL-001', 'SBI Home Loan', 700, 'SBI', 'HL', 'Floating', 650, 900, 0.085, 0, 60, 300, 1000000, 9999999999) RETURNING id INTO p_sbi_hl;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('SBI-LAP-001', 'SBI LAP', 700, 'SBI', 'LAP', 'Floating', 650, 900, 0.090, 0, 60, 180, 1000000, 9999999999) RETURNING id INTO p_sbi_lap;

    -- 2.9 Bajaj Prime (split by employment type for differentiated login fees)
    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-PRM-HL-SAL', 'Bajaj Prime HL Salaried', 300, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.085, 2000, 60, 300, 3500000, 100000000) RETURNING id INTO p_bajaj_prime_hl_sal;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-PRM-HL-SEP', 'Bajaj Prime HL SEP/SENP', 300, 'Bajaj Finance', 'HL', 'Floating', 650, 900, 0.085, 3500, 60, 300, 3500000, 100000000) RETURNING id INTO p_bajaj_prime_hl_sep;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-PRM-LAP-SAL', 'Bajaj Prime LAP Salaried', 300, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.090, 2000, 60, 180, 3500000, 100000000) RETURNING id INTO p_bajaj_prime_lap_sal;

    INSERT INTO loan_products (product_code, product_name, lender_id, lender_name, loan_type, interest_type, min_cibil, max_cibil, roi, login_fees, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount)
    VALUES ('BAJAJ-PRM-LAP-SEP', 'Bajaj Prime LAP SEP/SENP', 300, 'Bajaj Finance', 'LAP', 'Floating', 650, 900, 0.090, 3500, 60, 180, 3500000, 100000000) RETURNING id INTO p_bajaj_prime_lap_sep;

    -- ==========================================
    -- 3. PRODUCT ROI MATRIX
    -- ==========================================
    
    -- 3.1 Bajaj Near Prime Matrix (HL & LAP)
    -- Format: empType, minAmt, maxAmt, minCibil, maxCibil, isNtc, roi

    -- Salaried, <= 30 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 800, 900, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 780, 799, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 750, 779, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 730, 749, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 700, 729, false, 0.0975),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, 650, 700, false, 0.1050),
    (p_bajaj_np_hl, 'Salaried', 0, 3000000, null, null, true, 0.0975);

    -- Salaried, >30-75 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 800, 900, false, 0.0925),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 780, 799, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 750, 779, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 730, 749, false, 0.0950),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 700, 729, false, 0.0975),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, 650, 700, false, 0.1050),
    (p_bajaj_np_hl, 'Salaried', 3000001, 7500000, null, null, true, 0.0975);

    -- SENP, <= 30 Lakhs
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 800, 900, false, 0.0970),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 780, 799, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 750, 779, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 730, 749, false, 0.0995),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 700, 729, false, 0.1015),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, 650, 700, false, 0.1085),
    (p_bajaj_np_hl, 'SENP', 0, 3000000, null, null, true, 0.1015);

    -- 3.2 L&T Matrix
    -- Salaried 0-50L
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'Salaried', 0, 5000000, 800, 900, false, 0.0795),
    (p_lnt_hl, 'Salaried', 0, 5000000, 780, 799, false, 0.0820),
    (p_lnt_hl, 'Salaried', 0, 5000000, 750, 779, false, 0.0820),
    (p_lnt_hl, 'Salaried', 0, 5000000, 730, 749, false, 0.0835),
    (p_lnt_hl, 'Salaried', 0, 5000000, 700, 729, false, 0.0835),
    (p_lnt_hl, 'Salaried', 0, 5000000, 650, 700, false, 0.0890),
    (p_lnt_hl, 'Salaried', 0, 5000000, null, null, true, 0.0835);

    -- SEP/SENP 0-50L
    INSERT INTO product_roi_matrix (product_id, employment_type, min_loan_amount, max_loan_amount, min_cibil, max_cibil, is_ntc, roi) VALUES
    (p_lnt_hl, 'SEP', 0, 5000000, 800, 900, false, 0.0830),
    (p_lnt_hl, 'SEP', 0, 5000000, 780, 799, false, 0.0850),
    (p_lnt_hl, 'SEP', 0, 5000000, 750, 779, false, 0.0850),
    (p_lnt_hl, 'SEP', 0, 5000000, 730, 749, false, 0.0865),
    (p_lnt_hl, 'SEP', 0, 5000000, 700, 729, false, 0.0865),
    (p_lnt_hl, 'SEP', 0, 5000000, 650, 700, false, 0.0920),
    (p_lnt_hl, 'SEP', 0, 5000000, null, null, true, 0.0865);


    -- ==========================================
    -- 4. ELIGIBILITY CONDITIONS (LANES)
    -- ==========================================
    
    -- L&T HL Salaried NIP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, negative_property,
        negative_employer_type, negative_salary_mode, cibil_min, min_income, min_age, max_age,
        business_age_years, itr_required_years, conditions
    ) VALUES (
        'LNT-HL-001', p_lnt_hl, 'Salaried', 'NIP', 'Residential', 'Informal',
        'Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs', 'Cash, UPI', 650, 30000, 23, 60,
        null, 3, 'Net Monthly Income'
    );

    -- L&T HL SEP NIP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, negative_property,
        cibil_min, min_income, min_age, max_age, business_age_years, itr_required_years, conditions
    ) VALUES (
        'LNT-HL-001', p_lnt_hl, 'SEP/SENP', 'NIP', 'Residential', 'Informal',
        650, 25000, 25, 70, 3, 3, 'PAT+Depreciation+Interest'
    );
    
    -- ICICI HL Salaried NIP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, negative_property,
        negative_salary_mode, profile_restrictions, cibil_min, min_income, min_age, max_age,
        itr_required_years, conditions
    ) VALUES (
        'ICICI-HL-001', p_icici_hl, 'Salaried', 'NIP', 'Residential', 'Informal',
        'Cash, UPI, Cheque', 'Community Dominated Area', 700, 30000, 20, 62,
        3, 'Net Monthly Income'
    );

    -- ICICI HL SEP Banking
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, negative_property,
        profile_restrictions, cibil_min, min_income, min_age, max_age, business_age_years,
        itr_required_years, deviation_formulae, notes
    ) VALUES (
        'ICICI-HL-001', p_icici_hl, 'SEP/SENP', 'Banking', 'Residential', 'Informal',
        'CDA', 700, 30000, 20, 70, 2,
        3, 'ABB=EMI*3', 'Any no. of co-applicant and accounts'
    );

    -- ABFL HL Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('ABFL-HL-001', p_abfl_hl, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- ABFL HL SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('ABFL-HL-001', p_abfl_hl, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- ABFL LAP Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('ABFL-LAP-001', p_abfl_lap, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- ABFL LAP SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('ABFL-LAP-001', p_abfl_lap, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- BOB HL Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BOB-HL-001', p_bob_hl, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- BOB HL SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BOB-HL-001', p_bob_hl, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- BOB LAP Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BOB-LAP-001', p_bob_lap, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- BOB LAP SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BOB-LAP-001', p_bob_lap, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- SBI HL Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('SBI-HL-001', p_sbi_hl, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- SBI HL SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('SBI-HL-001', p_sbi_hl, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- SBI LAP Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('SBI-LAP-001', p_sbi_lap, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- SBI LAP SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('SBI-LAP-001', p_sbi_lap, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- BAJAJ PRIME HL Salaried (₹2,000 login fee)
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BAJAJ-PRM-HL-SAL', p_bajaj_prime_hl_sal, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- BAJAJ PRIME HL SEP (₹3,500 login fee)
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BAJAJ-PRM-HL-SEP', p_bajaj_prime_hl_sep, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- BAJAJ PRIME LAP Salaried (₹2,000 login fee)
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BAJAJ-PRM-LAP-SAL', p_bajaj_prime_lap_sal, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- BAJAJ NEAR PRIME HL Salaried
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BAJAJ-NP-HL', p_bajaj_np_hl, 'Salaried', 'NIP', 'Residential', 650, 30000, 21, 60);

    -- BAJAJ NEAR PRIME HL SEP
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BAJAJ-NP-HL', p_bajaj_np_hl, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

    -- BAJAJ PRIME LAP SEP (₹3,500 login fee)
    INSERT INTO eligibility_conditions (
        product_code, product_id, employment_type, surrogate, property_type, cibil_min, min_income, min_age, max_age
    ) VALUES ('BAJAJ-PRM-LAP-SEP', p_bajaj_prime_lap_sep, 'SEP/SENP', 'NIP', 'Residential', 650, 30000, 21, 65);

END $$;
