-- ═══════════════════════════════════════════════════════════════════════════════
-- 🧠 V49: Create Product FOIR Matrix and Drop Old FOIR Max
-- Generated from exact user sheet data
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE product_foir_matrix (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    employment_type VARCHAR(255),
    surrogate VARCHAR(100),
    min_salary DECIMAL(15,2),
    max_salary DECIMAL(15,2),
    foir DECIMAL(6,4),
    deviation DECIMAL(6,4),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_foir_matrix_product FOREIGN KEY (product_id) REFERENCES loan_products(id)
);

CREATE INDEX idx_product_foir_matrix_lookup ON product_foir_matrix (product_id, employment_type, surrogate);

ALTER TABLE eligibility_conditions DROP COLUMN IF EXISTS foir_max;

-- ── Seed FOIR Matrix Data ────────────────────────────────────────────────────

-- LT-HL-0001 (L&T Finance)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Salaried', 'NIP', 30000, 50000, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Salaried', 'NIP', 50001, 75000, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Salaried', 'NIP', 75001, 150000, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Salaried', 'NIP', 150001, 9999999, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 25000, 9999999, 0.85, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.55, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.65, NULL);

-- ICICI-HL-0001 (ICICI Bank)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Salaried', 'NIP', 30000, 60000, 0.5, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Salaried', 'NIP', 60001, 100000, 0.6, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Salaried', 'NIP', 100001, 200000, 0.65, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Salaried', 'NIP', 200001, 9999999, 0.7, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 200000, 9999999, 1.4, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.33, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.99, 0.05);

-- BANDHAN-HL-0001 (Bandhan Bank)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001'), 'Salaried', 'NIP', 15000, 9999999, 0.65, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 15000, 9999999, 0.65, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 1.0, NULL);

-- ABFL-HL-0001 (Aditya Birla Finance Limited)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ABFL-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.5, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ABFL-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ABFL-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 1.5, NULL);

-- BOB-HL-0001 (Bank of Baroda)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Salaried', 'NIP', 10000, 75000, 0.5, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Salaried', 'NIP', 75001, 300000, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Salaried', 'NIP', 300001, 9999999, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Self Employed Professional', 'NIP', 300001, 500000, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Self Employed Professional', 'NIP', 500001, 9999999, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 10000, 75000, 0.5, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 75001, 300000, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-HL-0001'), 'Self Employed Non Professional', 'NIP', 300001, 9999999, 0.7, NULL);

-- SBI-HL-0001 (SBI)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Salaried', 'NIP', 25000, 41666, 0.5, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Salaried', 'NIP', 41667, 66666, 0.6, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Salaried', 'NIP', 66667, 83333, 0.65, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Salaried', 'NIP', 83334, 9999999, 0.7, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 25000, 41666, 0.5, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 41667, 66666, 0.6, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 66667, 83333, 0.65, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 83334, 9999999, 0.7, 0.15);

-- BAJAJ-HL-0001 (Bajaj Prime)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001'), 'Salaried', 'NIP', NULL, NULL, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.0, 0.2);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 1.0, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.66, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.8, NULL);

-- YES-HL-0001 (Yes Bank)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Salaried', 'NIP', 40000, 100000, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Salaried', 'NIP', 100001, 9999999, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.0, 0.2);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Self Employed Professional', 'CPM SEP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.66, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.7, NULL);

-- HDFC-HL-0001 (HDFC)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-HL-0001'), 'Salaried', 'NIP', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.65, NULL);

-- JIO-HL-0001 (JIO Finance)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Salaried', 'NIP', 30000, 50000, 0.55, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Salaried', 'NIP', 50001, 100000, 0.65, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Salaried', 'NIP', 100001, 9999999, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.75, NULL);

-- IDBI-HL-0001 (IDBI)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDBI-HL-0001'), 'Salaried', 'NIP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDBI-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.7, NULL);

-- TATA-HL-0001 (TATA Capital)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'TATA-HL-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.0, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'TATA-HL-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 1.0, NULL);

-- LT-LAP-0001 (L&T Finance)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Salaried', 'NIP', 30000, 50000, 0.55, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Salaried', 'NIP', 50001, 75000, 0.65, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Salaried', 'NIP', 75001, 150000, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Salaried', 'NIP', 150001, 9999999, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 25000, 9999999, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.55, 0.1);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'LT-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.65, NULL);

-- ICICI-LAP-0001 (ICICI Bank)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Salaried', 'NIP', 30000, 60000, 0.5, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Salaried', 'NIP', 60001, 100000, 0.6, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Salaried', 'NIP', 100001, 200000, 0.65, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Salaried', 'NIP', 200001, 9999999, 0.7, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 200000, 9999999, 1.4, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.33, 0.05);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ICICI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.99, NULL);

-- BANDHAN-LAP-0001 (Bandhan Bank)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001'), 'Salaried', 'NIP', 15000, 9999999, 0.65, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 15000, 9999999, 0.65, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BANDHAN-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 1.0, NULL);

-- ABFL-LAP-0001 (Aditya Birla Finance Limited)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ABFL-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.5, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ABFL-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'ABFL-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 1.5, NULL);

-- BOB-LAP-0001 (Bank of Baroda)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Salaried', 'NIP', 10000, 75000, 0.5, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Salaried', 'NIP', 75001, 300000, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Salaried', 'NIP', 300001, 9999999, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Self Employed Professional', 'NIP', 300001, 500000, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Self Employed Professional', 'NIP', 500001, 9999999, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 10000, 75000, 0.5, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 75001, 300000, 0.6, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BOB-LAP-0001'), 'Self Employed Non Professional', 'NIP', 300001, 9999999, 0.7, NULL);

-- SBI-LAP-0001 (SBI)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-LAP-0001'), 'Salaried', 'NIP', 25000, 41666, 0.5, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-LAP-0001'), 'Salaried', 'NIP', 41667, 83333, 0.55, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-LAP-0001'), 'Salaried', 'NIP', 83334, 9999999, 0.6, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 25000, 41666, 0.5, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 41667, 83333, 0.55, 0.15);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'SBI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', 83334, 9999999, 0.6, 0.15);

-- BAJAJ-LAP-0001 (Bajaj Prime)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001'), 'Salaried', 'NIP', NULL, NULL, 1.0, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.0, 0.2);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 1.0, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.66, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'BAJAJ-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.8, NULL);

-- YES-LAP-0001 (Yes Bank)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Salaried', 'NIP', 40000, 100000, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Salaried', 'NIP', 100001, 9999999, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.0, 0.2);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Self Employed Professional', 'CPM SEP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.66, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'YES-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.7, NULL);

-- HDFC-LAP-0001 (HDFC)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-LAP-0001'), 'Salaried', 'NIP', NULL, NULL, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.7, 0.2);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'Banking', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'HDFC-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.65, NULL);

-- IDFC-LAP-0001 (IDFC)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDFC-LAP-0001'), 'Salaried', 'NIP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDFC-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.5, 0.2);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDFC-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.75, NULL);

-- JIO-LAP-0001 (JIO Finance)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-LAP-0001'), 'Salaried', 'NIP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.8, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-LAP-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 0.7, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'JIO-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'GST', NULL, NULL, 0.75, NULL);

-- IDBI-LAP-0001 (IDBI)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDBI-LAP-0001'), 'Salaried', 'NIP', NULL, NULL, 0.75, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'IDBI-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 0.7, NULL);

-- TATA-LAP-0001 (TATA Capital)
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'TATA-LAP-0001'), 'Self Employed Professional/Self Employed Non Professional', 'NIP', NULL, NULL, 1.0, NULL);
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation) VALUES ((SELECT id FROM loan_products WHERE product_code = 'TATA-LAP-0001'), 'Self Employed Professional', 'SEP', NULL, NULL, 1.0, NULL);
