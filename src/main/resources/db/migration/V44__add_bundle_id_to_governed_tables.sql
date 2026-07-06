-- V44__add_bundle_id_to_governed_tables.sql

ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS bundle_id VARCHAR(255) DEFAULT 'BASE';
ALTER TABLE product_roi_matrix ADD COLUMN IF NOT EXISTS bundle_id VARCHAR(255) DEFAULT 'BASE';
ALTER TABLE product_pf_matrix ADD COLUMN IF NOT EXISTS bundle_id VARCHAR(255) DEFAULT 'BASE';
ALTER TABLE product_login_fee_matrix ADD COLUMN IF NOT EXISTS bundle_id VARCHAR(255) DEFAULT 'BASE';
