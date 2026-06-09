-- Sync existing eligibility_conditions bank_name with lender_name from loan_products
UPDATE eligibility_conditions ec
SET bank_name = lp.lender_name
FROM loan_products lp
WHERE ec.product_code = lp.product_code
  AND (ec.bank_name IS NULL OR ec.bank_name = '');
