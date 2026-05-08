-- ═══════════════════════════════════════════════════════════════════════════════
-- V9: Dynamic ROI Computation Logic Column
-- ═══════════════════════════════════════════════════════════════════════════════
-- Adds a SpEL expression column to loan_products for multi-dimensional interest
-- rate resolution based on applicant parameters (CIBIL, Employment Type, Amount).
--
-- Variables injected by FinancialComputationEngine.resolveInterestRate():
--   #cibil      — Integer (applicant's CIBIL score)
--   #loanAmount — Double  (requested loan amount in INR)
--   #empType    — String  ('SALARIED', 'SEP', 'ANY')
--
-- Examples:
--   Flat rate:      '8.50'
--   CIBIL-tiered:   '#cibil >= 750 ? 8.20 : (#cibil >= 700 ? 8.50 : 8.90)'
--   Multi-dim:      '(#empType == ''SALARIED'' && #cibil >= 750) ? 8.20 : 8.90'
--
-- When non-null, the engine evaluates this INSTEAD of the static roi field.
-- When null, the engine falls back to product.roi (the static base rate).
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE loan_products
    ADD COLUMN IF NOT EXISTS roi_computation_logic VARCHAR(2000);

COMMENT ON COLUMN loan_products.roi_computation_logic IS
    'SpEL expression for dynamic ROI computation. Variables: #cibil (int), #loanAmount (double), #empType (string). Returns annual interest rate as decimal (e.g. 8.50).';
