-- ═══════════════════════════════════════════════════════════════════════════════
-- 🧠 V52: Remove LOW_LTV conditions and fix empty surrogates for Salaried
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. Remove LOW_LTV totally
DELETE FROM eligibility_conditions WHERE surrogate = 'LOW_LTV';

-- 2. Fix empty surrogates for Salaried to match FOIR matrices ('NIP')
UPDATE eligibility_conditions
SET surrogate = 'NIP'
WHERE employment_type = 'Salaried' AND (surrogate IS NULL OR surrogate = '');
