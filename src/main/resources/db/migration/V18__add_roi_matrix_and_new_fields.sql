-- ==============================================================================
-- V18: Production Data Fields & ROI Matrix
-- ==============================================================================
-- Adds the missing fields required to represent the real production data
-- and introduces the relational `product_roi_matrix` table to replace
-- the old SpEL-based dynamic pricing system.
-- ==============================================================================

-- 1. Add missing fields to `eligibility_conditions`
ALTER TABLE eligibility_conditions
    ADD COLUMN negative_employer_type TEXT,
    ADD COLUMN negative_salary_mode TEXT,
    ADD COLUMN margin_by_occupation TEXT,
    ADD COLUMN provident_fund_mandatory BOOLEAN;

-- 2. Add missing fields to `loan_products`
ALTER TABLE loan_products
    ADD COLUMN insurance_charges TEXT;

-- 3. Create the `product_roi_matrix` table
CREATE TABLE product_roi_matrix (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES loan_products(id) ON DELETE CASCADE,
    
    -- Dimensions (NULL means "any")
    employment_type VARCHAR(50),      -- 'SALARIED', 'SEP', etc.
    min_loan_amount NUMERIC(15, 2),   -- Inclusive minimum (e.g., 0, 3000000)
    max_loan_amount NUMERIC(15, 2),   -- Exclusive maximum (e.g., 3000000, 7500000, NULL)
    min_cibil INT,                    -- Inclusive minimum (e.g., 750)
    max_cibil INT,                    -- Inclusive maximum (e.g., 779)
    is_ntc BOOLEAN DEFAULT FALSE,     -- TRUE for "New To Credit" (CIBIL 0 or -1)
    
    -- Result
    roi NUMERIC(6, 4) NOT NULL,       -- e.g., 0.0825 for 8.25%
    
    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for efficient engine querying
CREATE INDEX idx_roi_matrix_lookup 
    ON product_roi_matrix(product_id, employment_type);
