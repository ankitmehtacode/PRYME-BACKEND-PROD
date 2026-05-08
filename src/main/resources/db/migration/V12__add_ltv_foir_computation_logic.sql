-- Add dynamic SpEL computation logic columns for LTV and FOIR
ALTER TABLE eligibility_conditions ADD COLUMN ltv_computation_logic text;
ALTER TABLE eligibility_conditions ADD COLUMN foir_computation_logic text;
