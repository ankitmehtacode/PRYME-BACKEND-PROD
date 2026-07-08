-- V58__seed_missing_foir_matrix.sql
-- ==============================================================================
-- Seeds the product_foir_matrix table for all active products that currently
-- have ZERO FOIR rows. Data sourced from PRYME FOIR workbook (2026-07-08).
--
-- For each (lender, loan_type), inserts ALL FOIR rows from the workbook into
-- every product of that lender/loan_type that has no FOIR data yet.
-- The engine's runtime matchEmploymentType + surrogate filter ensures only
-- the correct FOIR row is used for each applicant profile.
--
-- Special FOIR values:
--   140-LTV% (ICICI SEP/SENP) → stored as 0.6500 (assuming ~75% LTV)
--   (170-LTV)% (IndusInd SEP/SENP) → stored as 0.9500 (assuming ~75% LTV)
--   5-15% deviation (SBI) → stored as 0.0500 (conservative lower bound)
-- ==============================================================================

-- ══════════════════════════════════════════════════════════════════════════════
-- HL PRODUCTS (loan_type = 'HL')
-- Covers: *-HL-0002, *-HL-0003 products
-- ══════════════════════════════════════════════════════════════════════════════

-- ── Aditya Birla Finance Limited (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 0::numeric, 300000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 300001::numeric, 600000::numeric, 0.6500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 600001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.5000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 1.5000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Aditya Birla Finance Limited'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Bajaj Finance (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, 0.2000::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6600::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Bajaj Finance'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Bandhan Bank (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 15000::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 15000::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Bandhan Bank'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Bank of Baroda (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 10000::numeric, 75000::numeric, 0.5000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 75001::numeric, 300000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 300001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional', 'NIP', 300001::numeric, 500000::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional', 'NIP', 500001::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 10000::numeric, 75000::numeric, 0.5000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 75001::numeric, 300000::numeric, 0.6000::numeric, NULL::numeric),
    ('Self Employed Non Professional', 'NIP', 300001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Bank of Baroda'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── HDFC Bank (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'HDFC Bank'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── ICICI Bank (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 30000::numeric, 60000::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 60001::numeric, 100000::numeric, 0.6000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 100001::numeric, 200000::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 200001::numeric, NULL::numeric, 0.7000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 200000::numeric, NULL::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.3300::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.9900::numeric, 0.0500::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'ICICI Bank'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── IDBI (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'IDBI'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Indus Ind Bank (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 20000::numeric, 50000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 50001::numeric, 100001::numeric, 0.7000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 100001::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 20000::numeric, NULL::numeric, 0.9500::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Indus Ind Bank'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── JIO Finance (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 30000::numeric, 50000::numeric, 0.5500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 50001::numeric, 100000::numeric, 0.6500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 100001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'JIO Finance'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── L&T Finance (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 30000::numeric, 50000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 50001::numeric, 75000::numeric, 0.7000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 75001::numeric, 150000::numeric, 0.7500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 150001::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 25000::numeric, NULL::numeric, 0.8500::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.5500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'L&T Finance'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── SBI (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 25000::numeric, 41666::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 41667::numeric, 66666::numeric, 0.6000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 66667::numeric, 83333::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 83334::numeric, NULL::numeric, 0.7000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 25000::numeric, 41666::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 41667::numeric, 66666::numeric, 0.6000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 66667::numeric, 83333::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 83334::numeric, NULL::numeric, 0.7000::numeric, 0.0500::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'SBI'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Tata Capital (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Tata Capital'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── YES BANK (HL) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 40000::numeric, 100000::numeric, 0.7000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 100001::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, 0.2000::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional', 'CPM SEP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6600::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'YES BANK'
  AND lp.loan_type = 'HL'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ══════════════════════════════════════════════════════════════════════════════
-- LAP PRODUCTS (loan_type = 'LAP')
-- Covers: *-LAP-0002, *-LAP-0003, *-Secured-* products
-- ══════════════════════════════════════════════════════════════════════════════

-- ── Aditya Birla Finance Limited (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 0::numeric, 300000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 300001::numeric, 600000::numeric, 0.6500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 600001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.5000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 1.5000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Aditya Birla Finance Limited'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Bajaj Finance (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, 0.2000::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6600::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Bajaj Finance'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Bandhan Bank (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 15000::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 15000::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Bandhan Bank'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Bank of Baroda (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 10000::numeric, 75000::numeric, 0.5000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 75001::numeric, 300000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 300001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional', 'NIP', 300001::numeric, 500000::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional', 'NIP', 500001::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 10000::numeric, 75000::numeric, 0.5000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 75001::numeric, 300000::numeric, 0.6000::numeric, NULL::numeric),
    ('Self Employed Non Professional', 'NIP', 300001::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Bank of Baroda'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── HDFC Bank (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 0.7000::numeric, 0.2000::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'HDFC Bank'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── ICICI Bank (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 30000::numeric, 60000::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 60001::numeric, 100000::numeric, 0.6000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 100001::numeric, 200000::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 200001::numeric, NULL::numeric, 0.7000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 200000::numeric, NULL::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.3300::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.9900::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'ICICI Bank'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── IDBI (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'IDBI'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── IDFC (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.5000::numeric, 0.2000::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'IDFC'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Indus Ind Bank (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 20000::numeric, 50000::numeric, 0.6000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 50001::numeric, 100001::numeric, 0.7000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 100001::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 20000::numeric, NULL::numeric, 0.9500::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Indus Ind Bank'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── JIO Finance (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'JIO Finance'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── L&T Finance (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 30000::numeric, 50000::numeric, 0.5500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 50001::numeric, 75000::numeric, 0.6500::numeric, NULL::numeric),
    ('Salaried', 'NIP', 75001::numeric, 150000::numeric, 0.7000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 150001::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 25000::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.5500::numeric, 0.1000::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'L&T Finance'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── SBI (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 25000::numeric, 41666::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 41667::numeric, 83333::numeric, 0.5500::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 83334::numeric, NULL::numeric, 0.6000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 25000::numeric, 41666::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 41667::numeric, 83333::numeric, 0.5500::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 83334::numeric, NULL::numeric, 0.6000::numeric, 0.0500::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'SBI'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── Tata Capital (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', NULL::numeric, NULL::numeric, 0.6500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 1.0000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'Tata Capital'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── YES BANK (LAP) ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 40000::numeric, 100000::numeric, 0.7000::numeric, NULL::numeric),
    ('Salaried', 'NIP', 100001::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', NULL::numeric, NULL::numeric, 1.0000::numeric, 0.2000::numeric),
    ('Self Employed Professional', 'SEP', NULL::numeric, NULL::numeric, 0.8000::numeric, NULL::numeric),
    ('Self Employed Professional', 'CPM SEP', NULL::numeric, NULL::numeric, 0.7500::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.6600::numeric, NULL::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.7000::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'YES BANK'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ── ICICI HFC (LAP) — Using ICICI Bank LAP FOIR as proxy ──
INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
    ('Salaried', 'NIP', 30000::numeric, 60000::numeric, 0.5000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 60001::numeric, 100000::numeric, 0.6000::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 100001::numeric, 200000::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Salaried', 'NIP', 200001::numeric, NULL::numeric, 0.7000::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'NIP', 200000::numeric, NULL::numeric, 0.6500::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'Banking', NULL::numeric, NULL::numeric, 0.3300::numeric, 0.0500::numeric),
    ('Self Employed Professional/Self Employed Non Professional', 'GST', NULL::numeric, NULL::numeric, 0.9900::numeric, NULL::numeric)
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = 'ICICI HFC'
  AND lp.loan_type = 'LAP'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);

-- ══════════════════════════════════════════════════════════════════════════════
-- CLEANUP: Normalize surrogate CPM_SEP → CPM SEP in eligibility_conditions
-- The FOIR matrix uses 'CPM SEP' (space), but eligibility_conditions had
-- 'CPM_SEP' (underscore) for YES Bank 0002 products. Engine uses
-- equalsIgnoreCase which does NOT treat underscore as space.
-- ══════════════════════════════════════════════════════════════════════════════
UPDATE eligibility_conditions
SET surrogate = 'CPM SEP', updated_at = now()
WHERE surrogate = 'CPM_SEP';
