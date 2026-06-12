-- ==============================================================================
-- V28: Ingest FOIR Slabs mapped to EXACT existing schema bounds
-- ==============================================================================

DO $$
DECLARE
    rec RECORD;
BEGIN
    -- Wipe the dummy conditions so we don't have duplicates
    DELETE FROM eligibility_conditions;
    
    -- Ensure fallback FOIR on loan_products is safely set to 0.65 to fix UI 0% bug
    UPDATE loan_products SET max_emi_nmi_ratio = 0.6500 WHERE max_emi_nmi_ratio IS NULL;


        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 30000, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 50001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 75001, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 150001, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 25000, 0.85, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.55, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 30000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 60001, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 100001, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 200001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 200000, 0.9500, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.33, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.99, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 15000, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 15000, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.6500, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 1.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 10000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 75001, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 300001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 300001, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 500001, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 10000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 75001, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 300001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 25000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 41667, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 66667, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 83334, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 25000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 41667, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 66667, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 83334, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.66, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 40000, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 100001, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.66, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 30000, 0.55, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 50001, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 100001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDBI-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'TATA-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.6500, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'TATA-HL%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 30000, 0.55, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 50001, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 75001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 150001, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 25000, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.55, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'LT-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 30000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 60001, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 100001, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 200001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 200000, 0.9500, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.33, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ICICI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.99, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 15000, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 15000, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BANDHAN-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.6500, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'ABFL-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 1.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 10000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 75001, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 300001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 300001, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 500001, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 10000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 75001, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BOB-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 300001, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 25000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 41667, 0.55, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 83334, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 25000, 0.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 41667, 0.55, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'SBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 83334, 0.6, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.66, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'BAJAJ-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 40000, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 100001, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.66, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'YES-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'BANKING', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'HDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.65, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.5, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDFC-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.8, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'JIO-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'GST', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.75, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'IDBI-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.7, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'TATA-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 0.6500, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SALARIED_SEP']) AS emp;
    END LOOP;
        FOR rec IN SELECT id, product_code, lender_name, loan_type FROM loan_products WHERE product_code LIKE 'TATA-LAP%' LOOP
        INSERT INTO eligibility_conditions (product_id, product_code, employment_type, surrogate, min_income, foir_max, min_age, max_age, cibil_min, property_type, bank_name, loan_type, is_active)
        SELECT rec.id, rec.product_code, emp, 'NIP', 0, 1.0, 21, 65, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', rec.lender_name, rec.loan_type, true
        FROM unnest(ARRAY['SENP', 'SEP_SENP']) AS emp;
    END LOOP;

END $$;
