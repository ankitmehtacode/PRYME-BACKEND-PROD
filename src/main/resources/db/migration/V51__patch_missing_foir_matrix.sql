-- ═══════════════════════════════════════════════════════════════════════════════
-- 🧠 V51: Patch Missing FOIR Data for ABFL, TATA Capital, and Jio Finance
-- Adds missing salary slabs provided by user and handles Jio code mismatches
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. ABFL Salaried NIP (HL and LAP)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', 0, 300000, 0.60
FROM loan_products WHERE lender_name ILIKE '%Aditya Birla%' AND loan_type IN ('HL', 'LAP');

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', 300001, 600000, 0.65
FROM loan_products WHERE lender_name ILIKE '%Aditya Birla%' AND loan_type IN ('HL', 'LAP');

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', 600001, 9999999, 0.70
FROM loan_products WHERE lender_name ILIKE '%Aditya Birla%' AND loan_type IN ('HL', 'LAP');


-- 2. TATA Capital Salaried NIP (HL and LAP)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', NULL, NULL, 0.65
FROM loan_products WHERE lender_name ILIKE '%Tata Capital%' AND loan_type IN ('HL', 'LAP');


-- 3. JIO Finance (HL)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', 30000, 50000, 0.55
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', 50001, 100000, 0.65
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', 100001, 9999999, 0.70
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.80
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional', 'SEP', NULL, NULL, 0.70
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.75
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.75
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'HL';


-- 4. JIO Finance (LAP)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Salaried', 'NIP', NULL, NULL, 0.75
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'LAP';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.80
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'LAP';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional', 'SEP', NULL, NULL, 0.70
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'LAP';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.75
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'LAP';

INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir)
SELECT id, 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.75
FROM loan_products WHERE lender_name ILIKE '%JIO Finance%' AND loan_type = 'LAP';
