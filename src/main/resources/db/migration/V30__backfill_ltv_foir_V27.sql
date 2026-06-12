-- ==============================================================================
-- V30: Populate Default FOIR and LTV for Eligibility Conditions (Post-V27)
-- ==============================================================================
-- The V27 ingestion script re-seeded the eligibility_conditions table but did
-- not populate ltv_allowed and foir_max, leaving them NULL.
-- The Admin CRM UI (SettingsTab.tsx) reads these fields directly and displays
-- "0% / 0%" if they are NULL. This migration backfills default values.
-- ==============================================================================

UPDATE eligibility_conditions 
SET foir_max = 0.65 
WHERE foir_max IS NULL;

UPDATE eligibility_conditions 
SET ltv_allowed = 0.80 
WHERE ltv_allowed IS NULL;
