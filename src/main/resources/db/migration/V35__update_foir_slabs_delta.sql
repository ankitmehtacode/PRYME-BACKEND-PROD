-- ═══════════════════════════════════════════════════════════════════════════════
-- V35 — FOIR Slabs & Dynamic ICICI FOIR Updates
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. L&T Finance SEP FOIR max update to 75% (0.75) for HL and LAP
UPDATE eligibility_conditions 
SET foir_max = 0.75 
WHERE surrogate = 'SEP' AND (bank_name = 'L&T Finance' OR bank_name = 'L&T');

-- 2. Yes Bank SEP FOIR max update to 80% (0.80) for HL and LAP
UPDATE eligibility_conditions 
SET foir_max = 0.80 
WHERE surrogate = 'SEP' AND bank_name = 'Yes Bank';

-- 3. Yes Bank CPM_SEP FOIR max update to 75% (0.75) for HL and LAP
UPDATE eligibility_conditions 
SET foir_max = 0.75 
WHERE surrogate = 'CPM_SEP' AND bank_name = 'Yes Bank';

-- 4. Jio Finance SEP FOIR max update to 70% (0.70) for HL and LAP
UPDATE eligibility_conditions 
SET foir_max = 0.70 
WHERE surrogate = 'SEP' AND bank_name = 'JIO Finance';

-- 5. Tata Capital SEP FOIR max update to 100% (1.00) for HL and LAP
UPDATE eligibility_conditions 
SET foir_max = 1.00 
WHERE surrogate = 'SEP' AND (bank_name = 'Tata Capital' OR bank_name = 'TATA Capital');

-- 6. Bajaj Prime SEP FOIR max update to 100% (1.00) for HL and LAP
UPDATE eligibility_conditions 
SET foir_max = 1.00 
WHERE surrogate = 'SEP' AND bank_name = 'Bajaj Prime';

-- 7. ICICI Bank NIP SEP/SENP FOIR max update to NULL (triggers dynamic 1.40 - LTV check in Java)
UPDATE eligibility_conditions 
SET foir_max = NULL 
WHERE surrogate = 'NIP' 
  AND bank_name = 'ICICI Bank' 
  AND (employment_type = 'SENP' OR employment_type = 'SEP_SENP');

-- 8. Bank of Baroda NIP SEP vs SENP routing:
-- For Professional (SEP) lanes (foir_max 0.75 and 0.80), deny SENP (raw SELF_EMPLOYED)
UPDATE eligibility_conditions 
SET profile_restrictions = 'SELF_EMPLOYED' 
WHERE bank_name = 'Bank of Baroda' 
  AND surrogate = 'NIP' 
  AND (foir_max = 0.75 OR foir_max = 0.80);

-- For Non-Professional (SENP) lane (foir_max 0.70), deny SEP (raw PROFESSIONAL)
UPDATE eligibility_conditions 
SET profile_restrictions = 'PROFESSIONAL' 
WHERE bank_name = 'Bank of Baroda' 
  AND surrogate = 'NIP' 
  AND foir_max = 0.70;
