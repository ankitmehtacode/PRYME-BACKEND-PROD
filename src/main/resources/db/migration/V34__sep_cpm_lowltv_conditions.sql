-- ═══════════════════════════════════════════════════════════════════════════════
-- V34 — SEP, CPM_SEP, and Low LTV explicit eligibility conditions
-- ═══════════════════════════════════════════════════════════════════════════════
-- Adds new surrogate program lanes for:
--   1. SEP (Self Employed Professional) — Gross Receipt × multiplier × FOIR
--   2. CPM_SEP (Cash Profit Method) — (PAT + Depreciation) × multiplier
--   3. LOW_LTV — explicit condition rows with bank-specific LTV caps
-- ═══════════════════════════════════════════════════════════════════════════════

DO $$
DECLARE
    p_id BIGINT;
BEGIN

-- ═══════════════════════════════════════════════════════════════════════════════
-- SECTION 1: SEP SURROGATE CONDITIONS
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── L&T Finance HL — SEP ─────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'LT-HL-0002', 'SEP', 'SEP', 23, 60, 30000,
         3, 650, 'RESIDENTIAL, COMMERCIAL, PLOT', 'L&T Finance', 'HL', true,
         0.55, 0.75, 'CA/Doctor: 2.5x, CS: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── L&T Finance LAP — SEP ───────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'LT-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'LT-LAP-0002', 'SEP', 'SEP', 23, 60, 30000,
         3, 650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PLOT', 'L&T Finance', 'LAP', true,
         0.55, 0.75, 'CA/Doctor: 2.5x, CS: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Bajaj Prime HL — SEP ─────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'BAJAJ-HL-0002', 'SEP', 'SEP', 25, 70, 25000,
         3, 700, 'RESIDENTIAL, PLOT', 'Bajaj Prime', 'HL', true,
         0.65, 0.75, 'CA/Doctor: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Bajaj Prime LAP — SEP ───────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'BAJAJ-LAP-0002', 'SEP', 'SEP', 25, 70, 25000,
         3, 700, 'RESIDENTIAL, COMMERCIAL, PLOT', 'Bajaj Prime', 'LAP', true,
         0.65, 0.65, 'CA/Doctor: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Yes Bank HL — SEP ────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'YES-HL-0002', 'SEP', 'SEP', 21, 65, 25000,
         3, 700, 'RESIDENTIAL, PLOT', 'Yes Bank', 'HL', true,
         0.65, 0.90, 'Doctor/BDS/BHMS/BAMS: 2x, CA/CS/Architect: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Yes Bank LAP — SEP ──────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'YES-LAP-0002', 'SEP', 'SEP', 21, 65, 25000,
         3, 700, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'Yes Bank', 'LAP', true,
         0.65, 0.70, 'Doctor/BDS/BHMS/BAMS: 2x, CA/CS/Architect: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── JIO Finance HL — SEP ────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'JIO-HL-0001', 'SEP', 'SEP', 21, 65, 25000,
         3, 650, 'RESIDENTIAL, PLOT', 'JIO Finance', 'HL', true,
         0.65, 0.90, 'CA/Doctor: 3x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── JIO Finance LAP — SEP ───────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'JIO-LAP-0001', 'SEP', 'SEP', 21, 65, 25000,
         3, 650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PLOT', 'JIO Finance', 'LAP', true,
         0.65, 0.75, 'CA: 2x, Doctor: 3x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── TATA Capital HL — SEP ────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'TATA-HL-0002', 'SEP', 'SEP', 21, 65, 25000,
         3, 650, 'RESIDENTIAL, PLOT', 'Tata Capital', 'HL', true,
         0.65, 0.90, 'Doctor: 2.5x, CA/Architect: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;

-- ── TATA Capital LAP — SEP ──────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'TATA-LAP-0002', 'SEP', 'SEP', 21, 65, 25000,
         3, 650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL, PLOT', 'Tata Capital', 'LAP', true,
         0.65, 0.70, 'Doctor: 2.5x, CA/Architect: 1.5x', 'Gross Receipt*multiplier*FOIR')
    ON CONFLICT DO NOTHING;
END IF;


-- ═══════════════════════════════════════════════════════════════════════════════
-- SECTION 2: CPM_SEP SURROGATE CONDITIONS (Yes Bank only)
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── Yes Bank HL — CPM_SEP ────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'YES-HL-0002', 'SEP', 'CPM_SEP', 21, 65, 25000,
         3, 700, 'RESIDENTIAL, PLOT', 'Yes Bank', 'HL', true,
         0.65, 0.90, 'Doctor/BDS/BHMS/BAMS: 4x, CA/CS/Architect: 3x', '(PAT+Depreciation)*multiplier')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Yes Bank LAP — CPM_SEP ──────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age, min_income,
         business_age_years, cibil_min, property_type, bank_name, loan_type, is_active,
         foir_max, ltv_allowed, margin_by_occupation, conditions)
    VALUES
        (p_id, 'YES-LAP-0002', 'SEP', 'CPM_SEP', 21, 65, 25000,
         3, 700, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'Yes Bank', 'LAP', true,
         0.65, 0.70, 'Doctor/BDS/BHMS/BAMS: 4x, CA/CS/Architect: 3x', '(PAT+Depreciation)*multiplier, CA/CS/Architect<=GrossReceipt')
    ON CONFLICT DO NOTHING;
END IF;


-- ═══════════════════════════════════════════════════════════════════════════════
-- SECTION 3: LOW_LTV EXPLICIT ELIGIBILITY CONDITIONS
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── ABFL HL — Low LTV ────────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'ABFL-HL-0001', 'Salaried', 'LOW_LTV', 21, 58,
         650, 'RESIDENTIAL, PLOT', 'Aditya Birla Finance Limited', 'HL', true, 0.50,
         'Market Value*LTV, Max 35000000')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'ABFL-HL-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL, PLOT', 'Aditya Birla Finance Limited', 'HL', true, 0.50,
         'Market Value*LTV, Max 35000000')
    ON CONFLICT DO NOTHING;
END IF;

-- ── ABFL LAP — Low LTV ──────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'ABFL-LAP-0001', 'Salaried', 'LOW_LTV', 21, 58,
         650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'Aditya Birla Finance Limited', 'LAP', true, 0.40,
         'Market Value*LTV, Max 35000000')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'ABFL-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'ABFL-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'Aditya Birla Finance Limited', 'LAP', true, 0.40,
         'Market Value*LTV, Max 35000000')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Bajaj HL — Low LTV ──────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'BAJAJ-HL-0001', 'Salaried', 'LOW_LTV', 25, 70,
         700, 'RESIDENTIAL, PLOT', 'Bajaj Prime', 'HL', true, 0.65,
         'Market Value*LTV')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'BAJAJ-HL-0002', 'SEP_SENP', 'LOW_LTV', 25, 70,
         700, 'RESIDENTIAL, PLOT', 'Bajaj Prime', 'HL', true, 0.65,
         'Market Value*LTV')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Bajaj LAP — Low LTV (Residential 65%, Commercial 50%) ───────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'BAJAJ-LAP-0001', 'Salaried', 'LOW_LTV', 25, 70,
         700, 'RESIDENTIAL', 'Bajaj Prime', 'LAP', true, 0.65,
         'Market Value*LTV, 1 time ABB'),
        (p_id, 'BAJAJ-LAP-0001', 'Salaried', 'LOW_LTV', 25, 70,
         700, 'COMMERCIAL', 'Bajaj Prime', 'LAP', true, 0.50,
         'Market Value*LTV, 1 time ABB')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'BAJAJ-LAP-0002', 'SEP_SENP', 'LOW_LTV', 25, 70,
         700, 'RESIDENTIAL', 'Bajaj Prime', 'LAP', true, 0.65,
         'Market Value*LTV, 1 time ABB'),
        (p_id, 'BAJAJ-LAP-0002', 'SEP_SENP', 'LOW_LTV', 25, 70,
         700, 'COMMERCIAL', 'Bajaj Prime', 'LAP', true, 0.50,
         'Market Value*LTV, 1 time ABB')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Yes Bank HL — Low LTV ───────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'YES-HL-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         700, 'RESIDENTIAL, PLOT', 'Yes Bank', 'HL', true, 0.45,
         'Market Value*LTV, Max 15000000')
    ON CONFLICT DO NOTHING;
END IF;

-- ── Yes Bank LAP — Low LTV (Residential 45%, Commercial 40%, Industrial 30%)
SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'YES-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         700, 'RESIDENTIAL', 'Yes Bank', 'LAP', true, 0.45,
         'Market Value*LTV, Max 15000000'),
        (p_id, 'YES-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         700, 'COMMERCIAL', 'Yes Bank', 'LAP', true, 0.40,
         'Market Value*LTV, Max 15000000'),
        (p_id, 'YES-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         700, 'INDUSTRIAL', 'Yes Bank', 'LAP', true, 0.30,
         'Market Value*LTV, Max 15000000')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'YES-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'YES-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         700, 'RESIDENTIAL', 'Yes Bank', 'LAP', true, 0.45,
         'Market Value*LTV, Max 15000000'),
        (p_id, 'YES-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         700, 'COMMERCIAL', 'Yes Bank', 'LAP', true, 0.40,
         'Market Value*LTV, Max 15000000'),
        (p_id, 'YES-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         700, 'INDUSTRIAL', 'Yes Bank', 'LAP', true, 0.30,
         'Market Value*LTV, Max 15000000')
    ON CONFLICT DO NOTHING;
END IF;

-- ── JIO Finance HL — Low LTV ────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'JIO-HL-0001', 'Salaried', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL', 'JIO Finance', 'HL', true, 0.50,
         'Market Value*LTV, Max 30000000')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'JIO-HL-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL', 'JIO Finance', 'HL', true, 0.50,
         'Market Value*LTV, Max 30000000')
    ON CONFLICT DO NOTHING;
END IF;

-- ── JIO Finance LAP — Low LTV ───────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'JIO-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'JIO Finance', 'LAP', true, 0.50,
         'Market Value*LTV, Max 30000000')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'JIO-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'JIO-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'JIO Finance', 'LAP', true, 0.50,
         'Market Value*LTV, Max 30000000')
    ON CONFLICT DO NOTHING;
END IF;

-- ── TATA Capital HL — Low LTV (Ready Pos: 50%, Plot: 40%, 5% deviation) ─────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         deviation_formulae, conditions)
    VALUES
        (p_id, 'TATA-HL-0001', 'Salaried', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL', 'Tata Capital', 'HL', true, 0.50,
         '5% deviation allowed', 'Market Value*LTV'),
        (p_id, 'TATA-HL-0001', 'Salaried', 'LOW_LTV', 21, 65,
         650, 'PLOT', 'Tata Capital', 'HL', true, 0.40,
         '5% deviation allowed', 'Market Value*LTV')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-HL-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         deviation_formulae, conditions)
    VALUES
        (p_id, 'TATA-HL-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL', 'Tata Capital', 'HL', true, 0.50,
         '5% deviation allowed', 'Market Value*LTV'),
        (p_id, 'TATA-HL-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'PLOT', 'Tata Capital', 'HL', true, 0.40,
         '5% deviation allowed', 'Market Value*LTV')
    ON CONFLICT DO NOTHING;
END IF;

-- ── TATA Capital LAP — Low LTV (Res/Com/Ind: 50%, Plot: 40%, 5% deviation) ──
SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         deviation_formulae, conditions)
    VALUES
        (p_id, 'TATA-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'Tata Capital', 'LAP', true, 0.50,
         '5% deviation allowed', 'Market Value*LTV'),
        (p_id, 'TATA-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         650, 'PLOT', 'Tata Capital', 'LAP', true, 0.40,
         '5% deviation allowed', 'Market Value*LTV')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'TATA-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         deviation_formulae, conditions)
    VALUES
        (p_id, 'TATA-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'Tata Capital', 'LAP', true, 0.50,
         '5% deviation allowed', 'Market Value*LTV'),
        (p_id, 'TATA-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         650, 'PLOT', 'Tata Capital', 'LAP', true, 0.40,
         '5% deviation allowed', 'Market Value*LTV')
    ON CONFLICT DO NOTHING;
END IF;

-- ── IDFC LAP — Low LTV ──────────────────────────────────────────────────────
SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0001';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'IDFC-LAP-0001', 'Salaried', 'LOW_LTV', 21, 65,
         600, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'IDFC', 'LAP', true, 0.40,
         'Market Value*LTV, Max 15000000')
    ON CONFLICT DO NOTHING;
END IF;

SELECT id INTO p_id FROM loan_products WHERE product_code = 'IDFC-LAP-0002';
IF p_id IS NOT NULL THEN
    INSERT INTO eligibility_conditions
        (product_id, product_code, employment_type, surrogate, min_age, max_age,
         cibil_min, property_type, bank_name, loan_type, is_active, ltv_allowed,
         conditions)
    VALUES
        (p_id, 'IDFC-LAP-0002', 'SEP_SENP', 'LOW_LTV', 21, 65,
         600, 'RESIDENTIAL, COMMERCIAL, INDUSTRIAL', 'IDFC', 'LAP', true, 0.40,
         'Market Value*LTV, Max 15000000')
    ON CONFLICT DO NOTHING;
END IF;

END $$;
