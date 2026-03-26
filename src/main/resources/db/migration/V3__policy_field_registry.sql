-- Create policy_field_definitions table
CREATE TABLE policy_field_definitions (
    id                    BIGSERIAL PRIMARY KEY,
    field_key             VARCHAR(80) NOT NULL UNIQUE,
    display_name          VARCHAR(120) NOT NULL,
    field_type            VARCHAR(30) NOT NULL,
    entity_type           VARCHAR(40) NOT NULL,
    absolute_lower_bound  NUMERIC(20,6),
    absolute_upper_bound  NUMERIC(20,6),
    allowed_values        TEXT,
    unit                  VARCHAR(20),
    requires_reason       BOOLEAN NOT NULL DEFAULT FALSE,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    description           TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create policy_change_audits table
CREATE TABLE policy_change_audits (
    id                    BIGSERIAL PRIMARY KEY,
    entity_type           VARCHAR(40) NOT NULL,
    entity_id             BIGINT NOT NULL,
    field_key             VARCHAR(80) NOT NULL,
    old_value             TEXT,
    new_value             TEXT NOT NULL,
    changed_by_user_id    BIGINT NOT NULL,
    reason                TEXT,
    applied_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ip_address            VARCHAR(45),
    effective_from        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create indexes
CREATE INDEX idx_pcd_entity_type ON policy_field_definitions(entity_type);
CREATE INDEX idx_pca_entity ON policy_change_audits(entity_type, entity_id);
CREATE INDEX idx_pca_field_key ON policy_change_audits(field_key);
CREATE INDEX idx_pca_applied_at ON policy_change_audits(applied_at DESC);

-- Seed policy_field_definitions table
INSERT INTO policy_field_definitions (field_key, display_name, field_type, entity_type, absolute_lower_bound, absolute_upper_bound, allowed_values, unit, requires_reason, is_active, description) VALUES
('min_cibil', 'Minimum CIBIL Score', 'NUMERIC_RANGE', 'LOAN_PRODUCT', 300, 900, NULL, 'INR', FALSE, TRUE, 'Minimum required CIBIL score for the loan product.'),
('max_cibil', 'Maximum CIBIL Score', 'NUMERIC_RANGE', 'LOAN_PRODUCT', 300, 900, NULL, 'INR', FALSE, TRUE, 'Maximum allowed CIBIL score for the loan product.'),
('roi', 'Rate of Interest', 'PERCENTAGE', 'LOAN_PRODUCT', 0.05, 0.36, NULL, 'PERCENT', FALSE, TRUE, 'Rate of interest offered by the loan product.'),
('min_tenure_months', 'Minimum Tenure (Months)', 'INTEGER', 'LOAN_PRODUCT', 12, 60, NULL, 'MONTHS', FALSE, TRUE, 'Minimum tenure in months for the loan product.'),
('max_tenure_months', 'Maximum Tenure (Months)', 'INTEGER', 'LOAN_PRODUCT', 60, 360, NULL, 'MONTHS', FALSE, TRUE, 'Maximum tenure in months for the loan product.'),
('min_loan_amount', 'Minimum Loan Amount', 'NUMERIC_RANGE', 'LOAN_PRODUCT', 100000, 50000000, NULL, 'INR', FALSE, TRUE, 'Minimum loan amount offered by the loan product.'),
('max_loan_amount', 'Maximum Loan Amount', 'NUMERIC_RANGE', 'LOAN_PRODUCT', 100000, 150000000, NULL, 'INR', FALSE, TRUE, 'Maximum loan amount offered by the loan product.'),
('processing_fee', 'Processing Fee', 'PERCENTAGE', 'LOAN_PRODUCT', 0.0, 0.03, NULL, 'PERCENT', FALSE, TRUE, 'Processing fee percentage for the loan product.'),
('prepayment_charges', 'Prepayment Charges', 'PERCENTAGE', 'LOAN_PRODUCT', 0.0, 0.05, NULL, 'PERCENT', FALSE, TRUE, 'Charges for prepaying the loan product.'),
('foreclosure_charges', 'Foreclosure Charges', 'PERCENTAGE', 'LOAN_PRODUCT', 0.0, 0.05, NULL, 'PERCENT', FALSE, TRUE, 'Charges for foreclosing the loan product.'),
('max_emi_nmi_ratio', 'Max EMI to NMI Ratio', 'PERCENTAGE', 'LOAN_PRODUCT', 0.30, 1.00, NULL, 'PERCENT', FALSE, TRUE, 'Maximum ratio of EMI to Net Monthly Income for the loan product.'),
('ltv', 'Loan-to-Value Ratio', 'PERCENTAGE', 'LOAN_PRODUCT', 0.40, 0.90, NULL, 'PERCENT', FALSE, TRUE, 'Loan-to-value ratio for the loan product.'),
('itr_requirement_years', 'ITR Requirement (Years)', 'INTEGER', 'LOAN_PRODUCT', 0, 5, NULL, 'YEARS', FALSE, TRUE, 'Number of years of ITR required for the loan product.'),
('salary_slip_months', 'Salary Slip Requirement (Months)', 'INTEGER', 'LOAN_PRODUCT', 1, 12, NULL, 'MONTHS', FALSE, TRUE, 'Number of months of salary slips required for the loan product.'),
('gst_required_months', 'GST Required (Months)', 'INTEGER', 'LOAN_PRODUCT', 1, 24, NULL, 'MONTHS', FALSE, TRUE, 'Number of months of GST statements required for the loan product.'),
('bank_statement_months', 'Bank Statement Requirement (Months)', 'INTEGER', 'LOAN_PRODUCT', 3, 24, NULL, 'MONTHS', FALSE, TRUE, 'Number of months of bank statements required for the loan product.'),
('dpd_allowed', 'DPD Allowed', 'BOOLEAN', 'LOAN_PRODUCT', NULL, NULL, NULL, 'BOOLEAN', TRUE, TRUE, 'Whether DPD (Days Past Due) is allowed for the loan product.'),
('write_off_allowed', 'Write-off Allowed', 'BOOLEAN', 'LOAN_PRODUCT', NULL, NULL, NULL, 'BOOLEAN', TRUE, TRUE, 'Whether write-off is allowed for the loan product.'),
('settlement_allowed', 'Settlement Allowed', 'BOOLEAN', 'LOAN_PRODUCT', NULL, NULL, NULL, 'BOOLEAN', TRUE, TRUE, 'Whether settlement is allowed for the loan product.'),
('min_age', 'Minimum Age', 'INTEGER', 'ELIGIBILITY_CONDITION', 18, 25, NULL, 'YEARS', FALSE, TRUE, 'Minimum age requirement for eligibility.'),
('max_age', 'Maximum Age', 'INTEGER', 'ELIGIBILITY_CONDITION', 55, 75, NULL, 'YEARS', FALSE, TRUE, 'Maximum age requirement for eligibility.'),
('min_income', 'Minimum Income', 'NUMERIC_RANGE', 'ELIGIBILITY_CONDITION', 10000, 200000, NULL, 'INR', FALSE, TRUE, 'Minimum income required for eligibility.'),
('work_exp_years', 'Work Experience (Years)', 'INTEGER', 'ELIGIBILITY_CONDITION', 0, 5, NULL, 'YEARS', FALSE, TRUE, 'Required work experience in years for eligibility.'),
('business_age_years', 'Business Age (Years)', 'INTEGER', 'ELIGIBILITY_CONDITION', 1, 5, NULL, 'YEARS', FALSE, TRUE, 'Required business age in years for eligibility.'),
('cibil_min', 'Minimum CIBIL Score', 'NUMERIC_RANGE', 'ELIGIBILITY_CONDITION', 300, 900, NULL, 'INR', FALSE, TRUE, 'Minimum required CIBIL score for eligibility.'),
('foir_max', 'FOIR Maximum', 'PERCENTAGE', 'ELIGIBILITY_CONDITION', 0.30, 1.00, NULL, 'PERCENT', FALSE, TRUE, 'Maximum Frontier Offer to Income ratio allowed for eligibility.'),
('cibil_floor', 'CIBIL Floor', 'NUMERIC_RANGE', 'GENERAL_BANK_POLICY', 300, 750, NULL, 'INR', FALSE, TRUE, 'Minimum CIBIL score required by the bank policy.'),
('ltv_max', 'LTV Maximum', 'PERCENTAGE', 'GENERAL_BANK_POLICY', 0.40, 0.90, NULL, 'PERCENT', FALSE, TRUE, 'Maximum Loan-to-Value ratio allowed by the bank policy.'),
('tenure_min_years', 'Minimum Tenure (Years)', 'INTEGER', 'GENERAL_BANK_POLICY', 1, 5, NULL, 'YEARS', FALSE, TRUE, 'Minimum tenure in years allowed by the bank policy.'),
('tenure_max_years', 'Maximum Tenure (Years)', 'INTEGER', 'GENERAL_BANK_POLICY', 5, 30, NULL, 'YEARS', FALSE, TRUE, 'Maximum tenure in years allowed by the bank policy.'),
('emi_not_obligated_months', 'EMI Not Obligated Months', 'INTEGER', 'GENERAL_BANK_POLICY', 0, 24, NULL, 'MONTHS', FALSE, TRUE, 'Number of months EMI is not obligated by the bank policy.'),
('foir_allowed', 'FOIR Allowed', 'PERCENTAGE', 'SURROGATE_POLICY', 0.30, 1.90, NULL, 'PERCENT', FALSE, TRUE, 'Allowed Frontier Offer to Income ratio for the surrogate policy.'),
('business_vintage_min_years', 'Business Vintage Minimum (Years)', 'INTEGER', 'SURROGATE_POLICY', 1, 10, NULL, 'YEARS', FALSE, TRUE, 'Minimum business vintage in years required by the surrogate policy.');

-- Seed boolean fields with requires_reason=TRUE
INSERT INTO policy_field_definitions (field_key, display_name, field_type, entity_type, absolute_lower_bound, absolute_upper_bound, allowed_values, unit, requires_reason, is_active, description) VALUES
('dpd_allowed', 'DPD Allowed', 'BOOLEAN', 'LOAN_PRODUCT', NULL, NULL, NULL, 'BOOLEAN', TRUE, TRUE, 'Whether DPD (Days Past Due) is allowed for the loan product.'),
('write_off_allowed', 'Write-off Allowed', 'BOOLEAN', 'LOAN_PRODUCT', NULL, NULL, NULL, 'BOOLEAN', TRUE, TRUE, 'Whether write-off is allowed for the loan product.'),
('settlement_allowed', 'Settlement Allowed', 'BOOLEAN', 'LOAN_PRODUCT', NULL, NULL, NULL, 'BOOLEAN', TRUE, TRUE, 'Whether settlement is allowed for the loan product.');

-- Seed text/enum fields
INSERT INTO policy_field_definitions (field_key, display_name, field_type, entity_type, absolute_lower_bound, absolute_upper_bound, allowed_values, unit, requires_reason, is_active, description) VALUES
('occupation', 'Occupation', 'TEXT', 'ELIGIBILITY_CONDITION', NULL, NULL, NULL, 'TEXT', FALSE, TRUE, 'Occupation of the applicant for eligibility.'),
('employer_type', 'Employer Type', 'ENUM_LIST', 'ELIGIBILITY_CONDITION', NULL, NULL, 'SALARIED,SELF_EMPLOYED,BUSINESS_OWNER', 'TEXT', FALSE, TRUE, 'Type of employer for eligibility.'),
('nature_of_business', 'Nature of Business', 'TEXT', 'ELIGIBILITY_CONDITION', NULL, NULL, NULL, 'TEXT', FALSE, TRUE, 'Nature of business for eligibility.'),
('industry', 'Industry', 'ENUM_LIST', 'ELIGIBILITY_CONDITION', NULL, NULL, 'IT,AUTO,FMCG,RETAIL,MANUFACTURING', 'TEXT', FALSE, TRUE, 'Industry sector for eligibility.'),
('kyc_requirement', 'KYC Requirement', 'ENUM_LIST', 'LOAN_PRODUCT', NULL, NULL, 'AADHAAR,PAN,VOTER_ID,PASSPORT', 'TEXT', FALSE, TRUE, 'Required KYC documents for the loan product.'),
('income_proof', 'Income Proof', 'ENUM_LIST', 'LOAN_PRODUCT', NULL, NULL, 'SALARY_SLIP,BANK_STATEMENT,ITR,GST', 'TEXT', FALSE, TRUE, 'Required income proof documents for the loan product.'),
('residence_profile', 'Residence Profile', 'ENUM_LIST', 'ELIGIBILITY_CONDITION', NULL, NULL, 'OWNED,RENTED,PARENTSRESSED', 'TEXT', FALSE, TRUE, 'Residence profile of the applicant for eligibility.'),
('property_type', 'Property Type', 'ENUM_LIST', 'GENERAL_BANK_POLICY', NULL, NULL, 'RESIDENTIAL,COMMERCIAL,LAND', 'TEXT', FALSE, TRUE, 'Allowed property types by the bank policy.'),
('city_tier', 'City Tier', 'ENUM_LIST', 'ELIGIBILITY_CONDITION', NULL, NULL, 'TIER_1,TIER_2,TIER_3', 'TEXT', FALSE, TRUE, 'City tier for eligibility.'),
('employment_type', 'Employment Type', 'ENUM_LIST', 'ELIGIBILITY_CONDITION', NULL, NULL, 'SALARIED,SELF_EMPLOYED,BUSINESS_OWNER', 'TEXT', FALSE, TRUE, 'Type of employment for eligibility.'),
('income_type', 'Income Type', 'ENUM_LIST', 'ELIGIBILITY_CONDITION', NULL, NULL, 'FIXED_VARIABLE,SALARY,OCCUPATION_BASED', 'TEXT', FALSE, TRUE, 'Type of income for eligibility.'),
('obligation_treatment', 'Obligation Treatment', 'ENUM_LIST', 'LOAN_PRODUCT', NULL, NULL, 'STRICT,FLEXIBLE,NONE', 'TEXT', FALSE, TRUE, 'Treatment of obligations for the loan product.'),
('risk_category', 'Risk Category', 'ENUM_LIST', 'LOAN_PRODUCT', NULL, NULL, 'LOW,MEDIUM,HIGH', 'TEXT', FALSE, TRUE, 'Risk category of the loan product.'),
('program_name', 'Program Name', 'TEXT', 'SURROGATE_POLICY', NULL, NULL, NULL, 'TEXT', FALSE, TRUE, 'Name of the surrogate policy program.');
