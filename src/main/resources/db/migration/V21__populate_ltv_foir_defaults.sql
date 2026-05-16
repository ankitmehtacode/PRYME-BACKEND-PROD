-- ==============================================================================
-- V21: Populate Default FOIR and LTV for Eligibility Conditions
-- ==============================================================================
-- The V19 production seed data script intentionally omitted ltv_allowed 
-- and foir_max because the backend engine falls back to product defaults.
-- However, the Admin CRM UI (SettingsTab.tsx) reads these values directly 
-- from the eligibility_conditions row, resulting in a "0% / 0%" display.
-- This migration backfills representative defaults to fix the UI display.
-- ==============================================================================

UPDATE eligibility_conditions 
SET foir_max = 0.65 
WHERE foir_max IS NULL;

UPDATE eligibility_conditions 
SET ltv_allowed = 0.80 
WHERE ltv_allowed IS NULL;
