-- ═══════════════════════════════════════════════════════════════════════════════
-- V8: Piecewise Fee Computation Logic Column
-- ═══════════════════════════════════════════════════════════════════════════════
-- Adds a SpEL expression column to loan_products for dynamic processing fee
-- resolution. When non-null, FinancialComputationEngine evaluates this expression
-- instead of the static processing_fee percentage.
--
-- Examples:
--   Flat 0.30%:     '#loanAmount * 0.003'
--   Piecewise L&T:  '#loanAmount <= 20000000 ? 10000.00 : #loanAmount * 0.0025'
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE loan_products
    ADD COLUMN IF NOT EXISTS pf_computation_logic VARCHAR(500);

COMMENT ON COLUMN loan_products.pf_computation_logic IS
    'SpEL expression for dynamic fee computation. Variable: #loanAmount (double). Returns absolute fee in INR.';
