-- ═══════════════════════════════════════════════════════════════════════════════
-- V32: MASTER PRODUCT CONFIGURATION ENRICHMENT
-- ═══════════════════════════════════════════════════════════════════════════════
-- Scope: Add enrichment columns, clear stale data, seed master config from
--        production lender rate cards across 13 lenders × 2 loan types.
-- Strategy: DELETE-then-INSERT pattern for enrichment columns ONLY.
--           Base product data (name, tenure, amounts, CIBIL) is UNTOUCHED.
-- ═══════════════════════════════════════════════════════════════════════════════

-- ─────────────────────────────────────────────────────────────────────────────
-- PART 1: SCHEMA EVOLUTION
-- ─────────────────────────────────────────────────────────────────────────────

-- 1a. eligibility_conditions: New enrichment columns
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS vintage TEXT;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS bank_statement_requirement TEXT;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS salary_slip_requirement TEXT;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS gst_return_requirement TEXT;

-- 1b. eligibility_conditions: Widen emi_not_obligated from BOOLEAN → TEXT
--     (production data uses free-text like '6 Month old', 'If Loans are related to business')
ALTER TABLE eligibility_conditions
    ALTER COLUMN emi_not_obligated TYPE TEXT
    USING CASE WHEN emi_not_obligated::text = 'true' THEN 'Yes' ELSE NULL END;

-- 1c. loan_products: Widen fee columns from NUMERIC → TEXT
--     (production data uses ranges: 'Upto 2%', '2-4%', '9000-18000')
ALTER TABLE loan_products ALTER COLUMN prepayment_charges TYPE TEXT USING prepayment_charges::TEXT;
ALTER TABLE loan_products ALTER COLUMN foreclosure_charges TYPE TEXT USING foreclosure_charges::TEXT;
ALTER TABLE loan_products ALTER COLUMN stamp_duties TYPE TEXT USING stamp_duties::TEXT;
ALTER TABLE loan_products ALTER COLUMN legal_technical_charges TYPE TEXT USING legal_technical_charges::TEXT;
ALTER TABLE loan_products ALTER COLUMN other_expense TYPE TEXT USING other_expense::TEXT;

-- 1d. loan_products: New column
ALTER TABLE loan_products ADD COLUMN IF NOT EXISTS admin_fee TEXT;


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 2: CLEAR OLD ENRICHMENT DATA (base product data untouched)
-- ─────────────────────────────────────────────────────────────────────────────

UPDATE eligibility_conditions SET
    negative_property       = NULL,
    negative_employer_type  = NULL,
    margin_by_occupation    = NULL,
    deviation_formulae      = NULL,
    conditions              = NULL,
    profile_restrictions    = NULL,
    negative_salary_mode    = NULL,
    provident_fund_mandatory = NULL,
    emi_not_obligated       = NULL,
    itr_required_years      = NULL,
    vintage                 = NULL,
    bank_statement_requirement = NULL,
    salary_slip_requirement = NULL,
    gst_return_requirement  = NULL,
    notes                   = NULL;

UPDATE loan_products SET
    admin_fee               = NULL,
    insurance_charges       = NULL,
    legal_technical_charges = NULL,
    other_expense           = NULL,
    stamp_duties            = NULL,
    prepayment_charges      = NULL,
    foreclosure_charges     = NULL;


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 3: SEED LOAN PRODUCT FEE DATA (per lender × loan type)
-- ─────────────────────────────────────────────────────────────────────────────

-- L&T Finance
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='5000', stamp_duties='0.25%', prepayment_charges='Upto 2%', foreclosure_charges='Upto 2%' WHERE product_code LIKE 'LT-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='5000', stamp_duties='0.50%', prepayment_charges='Upto 2%', foreclosure_charges='Upto 2%' WHERE product_code LIKE 'LT-LAP%';

-- ICICI Bank
UPDATE loan_products SET admin_fee='5900', insurance_charges=NULL, legal_technical_charges='2500', other_expense='8000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'ICICI-HL%';
UPDATE loan_products SET admin_fee='5900', insurance_charges=NULL, legal_technical_charges='2500', other_expense='8000', stamp_duties='0.50%', prepayment_charges='Upto 4%', foreclosure_charges='Upto 4%' WHERE product_code LIKE 'ICICI-LAP%';

-- Bandhan Bank
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='1670', other_expense='0', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'BANDHAN-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='1670', other_expense='0', stamp_duties='0.50%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'BANDHAN-LAP%';

-- Aditya Birla Finance Limited
UPDATE loan_products SET admin_fee=NULL, insurance_charges='1.5% - 2.5%', legal_technical_charges='0', other_expense='4000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'ABFL-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges='1.5% - 2.5%', legal_technical_charges='0', other_expense='4000', stamp_duties='0.50%', prepayment_charges='2-4%', foreclosure_charges='2-4%' WHERE product_code LIKE 'ABFL-LAP%';

-- Bank of Baroda
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='10030', other_expense='5000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'BOB-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='10030', other_expense='5000 + Mortgage charge 2500-15000 per loan amount', stamp_duties='0.50%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'BOB-LAP%';

-- SBI
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='9000-18000', other_expense='7400', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'SBI-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='9000-18000', other_expense='4500', stamp_duties='0.50%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'SBI-LAP%';

-- Bajaj Finance
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='6000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'BAJAJ-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='6000', stamp_duties='0.50%', prepayment_charges='2%', foreclosure_charges='2%' WHERE product_code LIKE 'BAJAJ-LAP%';

-- YES BANK
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='6000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'YES-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='6000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'YES-LAP%';

-- HDFC Bank
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='5000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'HDFC-HL%';
UPDATE loan_products SET admin_fee='5900', insurance_charges=NULL, legal_technical_charges='2500', other_expense='8000', stamp_duties='0.50%', prepayment_charges='Upto 4%', foreclosure_charges='Upto 4%' WHERE product_code LIKE 'HDFC-LAP%';

-- JIO Finance
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='0', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'JIO-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='5000', stamp_duties='0.50%', prepayment_charges='Upto 4%', foreclosure_charges='Upto 4%' WHERE product_code LIKE 'JIO-LAP%';

-- IDBI
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='2950', other_expense='7000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'IDBI-HL%';
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='2950', other_expense='7000', stamp_duties='0.50%', prepayment_charges='Upto 4%', foreclosure_charges='Upto 4%' WHERE product_code LIKE 'IDBI-LAP%';

-- TATA Capital
UPDATE loan_products SET admin_fee='2000', insurance_charges='1.5%-3%', legal_technical_charges='0', other_expense='5000', stamp_duties='0.25%', prepayment_charges='0%', foreclosure_charges='0%' WHERE product_code LIKE 'TATA-HL%';
UPDATE loan_products SET admin_fee='2000', insurance_charges='1.5%-3%', legal_technical_charges='0', other_expense='5000', stamp_duties='0.50%', prepayment_charges='Upto 4%', foreclosure_charges='Upto 4%' WHERE product_code LIKE 'TATA-LAP%';

-- IDFC (LAP only)
UPDATE loan_products SET admin_fee=NULL, insurance_charges=NULL, legal_technical_charges='0', other_expense='5000', stamp_duties='0.50%', prepayment_charges='0', foreclosure_charges='0' WHERE product_code LIKE 'IDFC-LAP%';


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 4: SEED ELIGIBILITY CONDITIONS — HOME LOAN (HL)
-- ─────────────────────────────────────────────────────────────────────────────

-- ═══ L&T FINANCE HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'LT-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'LT-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,10,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Max 4 Banking accounts, under 12 Months EMI addback'
WHERE product_code LIKE 'LT-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old',
    margin_by_occupation='Service - 10%, Retailer - 12%, Wholesale - 8%, Manufacturer - 4%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='under 12 Months EMI addback'
WHERE product_code LIKE 'LT-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ ICICI BANK HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI, Cheque',
    profile_restrictions='Community Dominated Area',
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'ICICI-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'ICICI-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,10,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Any no. of co-applicant and accounts'
WHERE product_code LIKE 'ICICI-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation='Manufacturer - 6%', conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Any no. of co-applicant and accounts'
WHERE product_code LIKE 'ICICI-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ BANDHAN BANK HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Gross Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'BANDHAN-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BANDHAN-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BANDHAN-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation='10%', conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BANDHAN-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ ADITYA BIRLA FINANCE HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'ABFL-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'ABFL-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Average Daily Balance*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Max 3 Banking accounts'
WHERE product_code LIKE 'ABFL-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='24 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='No Downsize upto 2.5 cr'
WHERE product_code LIKE 'ABFL-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ BANK OF BARODA HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'BOB-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='If Loans are related to business', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BOB-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══ SBI HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes='6 Month Lock In Period'
WHERE product_code LIKE 'SBI-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='6 Month Lock In Period'
WHERE product_code LIKE 'SBI-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══ BAJAJ FINANCE HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'BAJAJ-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BAJAJ-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,10,15,20,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BAJAJ-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Trader, Service - 10%, Wholesale, Manufacturer - 8%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BAJAJ-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ YES BANK HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'YES-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'YES-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Average Daily Balance*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='12 Months old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'YES-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation='20%', conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'YES-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ HDFC BANK HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='12 Months old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'HDFC-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation/2+Interest',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='12 Months old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'HDFC-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='5 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='12 Months old',
    margin_by_occupation='Manufacturer, Trader - 8%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'HDFC-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ JIO FINANCE HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'JIO-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'JIO-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Average Daily Balance*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Months old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'JIO-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Trader - 6%, Manufacturer - 8%, Service - 12%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'JIO-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ IDBI HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'IDBI-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'IDBI-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══ TATA CAPITAL HL ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'TATA-HL%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'TATA-HL%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ─────────────────────────────────────────────────────────────────────────────
-- PART 5: SEED ELIGIBILITY CONDITIONS — LOAN AGAINST PROPERTY (LAP)
-- ─────────────────────────────────────────────────────────────────────────────

-- ═══ L&T FINANCE LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'LT-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'LT-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,10,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement=NULL, notes='Max 4 accounts, EMI addback under 12 Months old'
WHERE product_code LIKE 'LT-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Service - 10%, Retailer - 12%, Wholesale - 8%, Manufacturer - 4%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'LT-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ ICICI BANK LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI, Cheque',
    profile_restrictions='Community Dominated Area',
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'ICICI-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='Community Dominated Area',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'ICICI-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,10,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions='ABB=EMI*3',
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Any no. of co-applicant and accounts'
WHERE product_code LIKE 'ICICI-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin 6%',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation='Manufacturer - 6%', conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Any no. of co-applicant and accounts'
WHERE product_code LIKE 'ICICI-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ BANDHAN BANK LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Hospital, Industry, Marriage Garden', deviation_formulae='Gross Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL,
    conditions='If Tenure=20 Years, ROI+0.20%',
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'BANDHAN-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Hospital, Industry, Marriage Garden', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL,
    conditions='If Tenure=20 Years, ROI+0.20%',
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BANDHAN-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Hospital, Industry, Marriage Garden', deviation_formulae='ABB - 5,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL,
    conditions='If Tenure=20 Years, ROI+0.20%',
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BANDHAN-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Hospital, Industry, Marriage Garden', deviation_formulae='12 Months Turnover*Margin 10%',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation='10%',
    conditions='If Tenure=20 Years, ROI+0.20%',
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BANDHAN-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ ADITYA BIRLA FINANCE LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Special Property', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'ABFL-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Special Property', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'ABFL-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Special Property', deviation_formulae='Average Daily Balance*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage=NULL, itr_required_years=NULL, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement=NULL, notes='Max 3 Banking accounts'
WHERE product_code LIKE 'ABFL-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Special Property', deviation_formulae='24 Months Turnover',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='24 Months', notes='No Downsize upto 2.5 cr'
WHERE product_code LIKE 'ABFL-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ BANK OF BARODA LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Hospital/School', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash',
    profile_restrictions='Builders',
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL,
    conditions='If Tenure upto 15, ROI Increases',
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes='Commitment charge - 0.5%'
WHERE product_code LIKE 'BOB-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Hospital/School', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='Builders',
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='If Loans are related to business', margin_by_occupation=NULL,
    conditions='If Tenure upto 15, ROI Increases',
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Commitment charge - 0.5%'
WHERE product_code LIKE 'BOB-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══ SBI LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Hospital/School', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes='6 Month Lock In Period'
WHERE product_code LIKE 'SBI-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Hospital/School', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='6 Month Lock In Period'
WHERE product_code LIKE 'SBI-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══ BAJAJ FINANCE LAP ═══

UPDATE eligibility_conditions SET
    negative_property='NIL', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'BAJAJ-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='NIL', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BAJAJ-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='NIL', deviation_formulae='ABB - 5,10,15,20,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BAJAJ-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='NIL', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Trader, Service - 10%, Wholesale, Manufacturer - 8%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'BAJAJ-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ YES BANK LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash', profile_restrictions=NULL,
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'YES-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'YES-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Average Daily Balance*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='12 Months old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'YES-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation='20%', conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'YES-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ HDFC BANK LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI, Cheque',
    profile_restrictions='Community Dominated Area',
    vintage=NULL, itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'HDFC-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='Community Dominated Area',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'HDFC-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='ABB - 5,10,15,25*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Month old', margin_by_occupation=NULL, conditions='ABB=EMI*3',
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Any no. of co-applicant and accounts'
WHERE product_code LIKE 'HDFC-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL,
    profile_restrictions='CDA',
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Trader - 9%, Manufacturer - 10%, Service - 8%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes='Upto 5cr no financials required'
WHERE product_code LIKE 'HDFC-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ IDFC LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'IDFC-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'IDFC-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Manufacturer - 10%, Trader, Service - 7%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'IDFC-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ JIO FINANCE LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type=NULL, negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'JIO-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'JIO-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Average Daily Balance*FOIR',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated='6 Months old', margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'JIO-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='BANKING';

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='12 Months Turnover*Margin',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL,
    margin_by_occupation='Trader - 6%, Manufacturer - 8%, Service - 12%',
    conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'JIO-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='GST';


-- ═══ IDBI LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'IDBI-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'IDBI-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══ TATA CAPITAL LAP ═══

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='Net Monthly Income',
    negative_employer_type='Proprietorship, Partnership, Trusts, AoPs, BoIs, NGOs',
    negative_salary_mode='Cash, UPI', profile_restrictions=NULL,
    vintage='2 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement='3 Months',
    gst_return_requirement=NULL, notes=NULL
WHERE product_code LIKE 'TATA-LAP%' AND employment_type IN ('Salaried', 'SALARIED_SEP') AND (surrogate='NIP' OR surrogate IS NULL);

UPDATE eligibility_conditions SET
    negative_property='Informal', deviation_formulae='PAT+Depreciation+Interest',
    negative_employer_type=NULL, negative_salary_mode=NULL, profile_restrictions=NULL,
    vintage='3 Years', itr_required_years=3, provident_fund_mandatory=false,
    emi_not_obligated=NULL, margin_by_occupation=NULL, conditions=NULL,
    bank_statement_requirement='12 Months', salary_slip_requirement=NULL,
    gst_return_requirement='12 Months', notes=NULL
WHERE product_code LIKE 'TATA-LAP%' AND employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP') AND surrogate='NIP';


-- ═══════════════════════════════════════════════════════════════════════════════
-- END V32
-- ═══════════════════════════════════════════════════════════════════════════════
