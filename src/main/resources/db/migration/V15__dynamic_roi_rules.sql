-- ─────────────────────────────────────────────────────────────────────────────
-- V15: Dynamic ROI Rules for L&T Finance
-- ─────────────────────────────────────────────────────────────────────────────
-- Sets the `roi_computation_logic` SpEL expression based on the production matrix:
-- Evaluates CIBIL, Loan Amount, Employment Type, and Program Name (Surrogate vs Standard)
-- ─────────────────────────────────────────────────────────────────────────────

ALTER TABLE loan_products ALTER COLUMN roi_computation_logic TYPE TEXT;

UPDATE loan_products
SET roi_computation_logic = 
'((#programName == null || #programName == ''ANY'' || #programName == ''STANDARD'') ? ' ||
    '(#empType == ''SALARIED'' ? ' ||
        '(#cibil >= 800 ? (#loanAmount <= 5000000 ? 7.95 : (#loanAmount <= 10000000 ? 7.80 : (#loanAmount <= 15000000 ? 7.80 : 7.75))) : ' ||
        '(#cibil >= 750 ? (#loanAmount <= 5000000 ? 8.20 : (#loanAmount <= 10000000 ? 8.15 : (#loanAmount <= 15000000 ? 8.00 : 7.95))) : ' ||
        '(#cibil >= 700 ? (#loanAmount <= 5000000 ? 8.35 : (#loanAmount <= 10000000 ? 8.30 : (#loanAmount <= 15000000 ? 8.15 : 8.10))) : ' ||
        '(#loanAmount <= 5000000 ? 8.90 : (#loanAmount <= 10000000 ? 8.80 : (#loanAmount <= 15000000 ? 8.65 : 8.60)))))) ' ||
    ': ' ||
        '(#cibil >= 800 ? (#loanAmount <= 5000000 ? 8.30 : (#loanAmount <= 10000000 ? 8.25 : (#loanAmount <= 15000000 ? 8.25 : 8.20))) : ' ||
        '(#cibil >= 750 ? (#loanAmount <= 5000000 ? 8.50 : (#loanAmount <= 10000000 ? 8.45 : (#loanAmount <= 15000000 ? 8.30 : 8.25))) : ' ||
        '(#cibil >= 700 ? (#loanAmount <= 5000000 ? 8.65 : (#loanAmount <= 10000000 ? 8.60 : (#loanAmount <= 15000000 ? 8.45 : 8.40))) : ' ||
        '(#loanAmount <= 5000000 ? 9.20 : (#loanAmount <= 10000000 ? 9.10 : (#loanAmount <= 15000000 ? 8.95 : 8.90)))))) ' ||
') : (' ||
    '(#empType == ''SALARIED'' ? ' ||
        '(#cibil >= 800 ? (#loanAmount <= 5000000 ? 8.25 : (#loanAmount <= 10000000 ? 8.20 : (#loanAmount <= 15000000 ? 8.20 : 8.15))) : ' ||
        '(#cibil >= 750 ? (#loanAmount <= 5000000 ? 8.45 : (#loanAmount <= 10000000 ? 8.40 : (#loanAmount <= 15000000 ? 8.25 : 8.20))) : ' ||
        '(#cibil >= 700 ? (#loanAmount <= 5000000 ? 8.60 : (#loanAmount <= 10000000 ? 8.55 : (#loanAmount <= 15000000 ? 8.40 : 8.35))) : ' ||
        '(#loanAmount <= 5000000 ? 9.15 : (#loanAmount <= 10000000 ? 9.05 : (#loanAmount <= 15000000 ? 8.90 : 8.85)))))) ' ||
    ': ' ||
        '(#cibil >= 800 ? (#loanAmount <= 5000000 ? 8.40 : (#loanAmount <= 10000000 ? 8.35 : (#loanAmount <= 15000000 ? 8.35 : 8.30))) : ' ||
        '(#cibil >= 750 ? (#loanAmount <= 5000000 ? 8.60 : (#loanAmount <= 10000000 ? 8.55 : (#loanAmount <= 15000000 ? 8.40 : 8.35))) : ' ||
        '(#cibil >= 700 ? (#loanAmount <= 5000000 ? 8.75 : (#loanAmount <= 10000000 ? 8.70 : (#loanAmount <= 15000000 ? 8.55 : 8.50))) : ' ||
        '(#loanAmount <= 5000000 ? 9.30 : (#loanAmount <= 10000000 ? 9.20 : (#loanAmount <= 15000000 ? 9.05 : 9.00)))))) ' ||
'))'
WHERE product_code = 'LT_HL_001';
