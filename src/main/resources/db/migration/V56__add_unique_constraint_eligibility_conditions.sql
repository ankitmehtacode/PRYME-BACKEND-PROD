-- ==============================================================================
-- Root-cause fix: eligibility_conditions has never had a uniqueness guarantee on
-- (product_code, employment_type, surrogate). That is why 24 logical conditions
-- ended up with 2-5 conflicting rows each (mostly stale FOIR income-slab values
-- mistakenly inserted as separate condition rows), and why prior corrective
-- migrations (V36, V42) had no way to target "the one row for this condition" --
-- they fell back to fragile free-text WHERE clauses on bank_name/loan_type that
-- silently matched zero rows when the literal didn't match exactly.
--
-- This migration is a one-time cleanup + guardrail:
--   1. Normalize blank/NULL surrogate to 'NIP' (Postgres treats every NULL as
--      distinct, so a UNIQUE constraint would not catch NULL-surrogate dupes).
--   2. Deduplicate: for any (product_code, employment_type, surrogate) with
--      multiple rows, keep only the row with the LOWEST id, delete the rest.
--      (The authoritative values for what SHOULD be kept are re-asserted by
--      V51 immediately after, via upsert -- this step just clears the way for
--      the constraint to be added.)
--   3. Add the unique constraint so this class of bug cannot recur silently.
-- ==============================================================================

UPDATE eligibility_conditions
SET surrogate = 'NIP'
WHERE surrogate IS NULL OR btrim(surrogate) = '';

DELETE FROM eligibility_conditions a
USING eligibility_conditions b
WHERE a.id > b.id
  AND a.product_code = b.product_code
  AND a.employment_type = b.employment_type
  AND a.surrogate = b.surrogate;

ALTER TABLE eligibility_conditions
  ADD CONSTRAINT uq_elig_cond_product_emp_surrogate
  UNIQUE (product_code, employment_type, surrogate);
