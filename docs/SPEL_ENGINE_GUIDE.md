# 🧠 PRYME SpEL Engine Guide
> **Audience:** Admins configuring Engine Rules via the Admin Dashboard.  
> **Last updated:** June 2026

---

## 1. What is SpEL and Why Does PRYME Use It?

**SpEL** (Spring Expression Language) is a runtime expression evaluator built into Spring Boot.
In PRYME's Policy Matrix engine, SpEL is used for **conditional logic that must vary per applicant** — things a single static field cannot express.

Think of it this way:

| Tool | Question it answers |
|---|---|
| **Static fields** (minAge, ltvAllowed, foirMax…) | *Can this applicant get this loan?* (binary gate) |
| **SpEL expressions** (Pricing Matrix, Conditions, Deviations) | *What terms/limits does this specific applicant get?* (dynamic output) |

A static `ltvAllowed = 0.80` says: *"The maximum LTV for this lane is 80%."*  
A SpEL expression on the Dynamic LTV Matrix says: *"But if CIBIL ≥ 750, make it 85%."*

---

## 2. Where SpEL Appears in the Engine Rule Form

There are **four SpEL-powered fields** in the Add/Edit Engine Rule modal:

| Field | What it computes | Fallback if empty |
|---|---|---|
| **Dynamic LTV Matrix** | Effective LTV % for this applicant | Uses `ltvAllowed` static field |
| **Dynamic FOIR Matrix** | Effective FOIR % for this applicant | Uses `foirMax` static field |
| **Conditions** | Custom boolean rule (pass/fail gate) | Always passes |
| **Deviation Formulae** | Boolean override rule for deviations | Always passes |

---

## 3. The Available Variables (What SpEL Can See)

When the engine evaluates any SpEL expression, it injects the following **applicant context** variables. These are the only values you can reference:

| Variable | Type | Description | Example value |
|---|---|---|---|
| `#cibilScore` | `Integer` | Applicant's CIBIL score | `750` |
| `#loanAmount` | `BigDecimal` | Requested loan amount (₹) | `5000000` |
| `#propertyValue` | `BigDecimal` | Property value (₹) | `7500000` |
| `#existingEmiTotal` | `BigDecimal` | Sum of current EMIs (₹/month) | `15000` |
| `#applicantAge` | `Integer` | Age in years | `35` |
| `#employmentType` | `String` | `SALARIED` / `SELF_EMPLOYED_*` | `"SALARIED"` |
| `#workExpYears` | `Integer` | Years of work experience | `5` |
| `#businessAgeYears` | `Integer` | Years in business (SEP) | `3` |
| `#cityTier` | `String` | City tier (`TIER_1`, `TIER_2`) | `"TIER_1"` |
| `#propertyType` | `String` | Property sub-type | `"FLAT"` |

> **Note:** The `#cibil` shorthand also works in the Pricing Matrix Builder (compiled automatically by the UI).

---

## 4. SpEL Syntax Basics

SpEL expressions are written as **Java-like conditional expressions**. The most common pattern is the **ternary chain**:

```
(condition) ? value_if_true : value_if_false
```

You can nest them to create a priority list:

```
(condition_1) ? value_1 : ((condition_2) ? value_2 : default_value)
```

### Operators

| Operator | Meaning | Example |
|---|---|---|
| `==` | Equals | `#employmentType == 'SALARIED'` |
| `!=` | Not equals | `#employmentType != 'SALARIED'` |
| `>` | Greater than | `#cibilScore > 750` |
| `>=` | Greater than or equal | `#cibilScore >= 700` |
| `<` | Less than | `#loanAmount < 5000000` |
| `<=` | Less than or equal | `#applicantAge <= 60` |
| `&&` | AND (both must be true) | `#cibilScore >= 750 && #loanAmount <= 5000000` |
| `\|\|` | OR (either must be true) | `#cibilScore >= 750 \|\| #employmentType == 'SALARIED'` |
| `!` | NOT | `!#employmentType == 'SALARIED'` |

### String values must be in single quotes
```
#employmentType == 'SALARIED'   ✅  correct
#employmentType == "SALARIED"   ❌  will fail
```

---

## 5. Full Worked Example: HDFC Home Loan — SALARIED Lane

Let's walk through how an admin would configure a complete engine rule for **HDFC Home Loan, Salaried lane** with tiered LTV and FOIR based on CIBIL score.

### The Policy (Plain English)
> - Base LTV: 80%, Base FOIR: 65%
> - If CIBIL ≥ 750 → LTV goes up to 85%, FOIR up to 70%
> - If CIBIL ≥ 700 but < 750 → LTV stays 80%, FOIR stays 65%
> - If CIBIL < 700 → LTV drops to 75%, FOIR drops to 60%
> - Additionally: Deny the loan if the applicant has existing EMIs > ₹50,000/month

---

### Step 1: Static Fields (Gate Configuration)

These are the hard minimums / defaults set directly in the modal:

| Field | Value | Meaning |
|---|---|---|
| Employment Type | `SALARIED` | This lane only for salaried |
| Min Age | `21` | Must be at least 21 |
| Max Age | `65` | Must not exceed 65 |
| Min Income | `₹25,000` | Minimum monthly income |
| Vintage (Years) | `2` | Min 2 years work experience |
| ITR Required | `2` | Must have 2 years of ITR |
| EMI Not Obligated | `No` | EMIs are counted in FOIR |
| **Base LTV Allowed** | `80` | Baseline LTV (default for all) |
| **Base FOIR Allowed** | `65` | Baseline FOIR (default for all) |

---

### Step 2: Dynamic LTV Matrix (SpEL — tier-based override)

In the **"Dynamic LTV Matrix"** section, click `+ Add Pricing Tier` and configure:

| Employment | Min CIBIL | Max CIBIL | Min Amount | Max Amount | LTV (%) |
|---|---|---|---|---|---|
| Any | 750 | — | — | — | 85 |
| Any | 700 | 749 | — | — | 80 |
| Any | — | 699 | — | — | 75 |

The UI compiles this into the following **SpEL expression** (visible in the Live SpEL Output preview):

```spel
(#cibil >= 750) ? 85 : ((#cibil >= 700 && #cibil <= 749) ? 80 : 75)
```

**What this means at runtime:**
- Applicant A, CIBIL 780 → LTV = **85%** (best tier)
- Applicant B, CIBIL 725 → LTV = **80%** (middle tier)
- Applicant C, CIBIL 680 → LTV = **75%** (lowest tier)

> ⚠️ The LTV output is in **percentage form** (e.g. `85`), NOT decimal. The engine handles the conversion internally when computing `propertyValue × LTV`.

---

### Step 3: Dynamic FOIR Matrix (SpEL — tier-based override)

Similarly, in the **"Dynamic FOIR Matrix"** section:

| Employment | Min CIBIL | Max CIBIL | Min Amount | Max Amount | FOIR (%) |
|---|---|---|---|---|---|
| Any | 750 | — | — | — | 70 |
| Any | — | 699 | — | — | 60 |

Compiled SpEL:

```spel
(#cibil >= 750) ? 70 : ((#cibil < 700) ? 60 : 65)
```

**At runtime:**
- CIBIL 780 → FOIR = **70%** → Can carry more EMI
- CIBIL 680 → FOIR = **60%** → Stricter EMI cap
- CIBIL 720 → FOIR = **65%** (fallback = base rate)

---

### Step 4: Conditions (SpEL Boolean Gate)

In the **Conditions** text area, type a rule that acts as an additional pass/fail gate:

```spel
SPEL: #existingEmiTotal <= 50000
```

**What this does:** If the applicant's existing EMIs exceed ₹50,000/month, this condition evaluates to `false` and **the entire lane is rejected** for this applicant, even if all static fields passed.

> The `SPEL:` prefix is required for explicit SpEL in free-text fields. Without it, the engine tries to parse it as a deny-list or memo text.

---

### Step 5: Deviation Formulae (SpEL Boolean Gate)

For more complex approval overrides. Example: Allow deviation only if property is residential AND loan amount is under ₹1 crore:

```spel
SPEL: (#propertyType == 'FLAT' || #propertyType == 'APARTMENT') && #loanAmount <= 10000000
```

If this evaluates to `false`, the deviation is rejected.

---

## 6. How the Engine Evaluates All of This (Full Flow)

```
Applicant Request
      │
      ▼
[Static Gate Checks]
  Age ✓, Vintage ✓, ITR ✓, Min Income ✓
      │
      ▼ (all pass)
[SpEL Conditions evaluated]
  #existingEmiTotal <= 50000  →  true ✓
      │
      ▼
[SpEL Deviation Formulae evaluated]
  property is FLAT && amount <= 10000000  →  true ✓
      │
      ▼ (lane is MATCHED)
[Dynamic LTV Matrix SpEL evaluated]
  CIBIL=750 → LTV = 85%  →  effectiveLtv = 0.85
      │
[Dynamic FOIR Matrix SpEL evaluated]
  CIBIL=750 → FOIR = 70%  →  effectiveFoir = 0.70
      │
[Loan Calculation]
  finalLoanAmount = min(requested, propertyValue × 0.85, maxByFoir)
  processingFee   = loanAmount × staticRate
      │
      ▼
  ✅ EligibilityResult returned to user
```

---

## 7. Common Mistakes to Avoid

| Mistake | What happens | Fix |
|---|---|---|
| Using double quotes for strings | Expression throws parse error, lane fails | Use single quotes: `'SALARIED'` |
| Forgetting `SPEL:` prefix in Conditions/Deviations | Engine treats it as a memo (always passes) | Add `SPEL:` at the start |
| Using `%` in LTV/FOIR output (e.g. `0.85`) | The engine double-converts → LTV becomes 0.0085 | Return integer percent: `85`, not `0.85` |
| Leaving a Conditions field blank | Always passes — fine if intentional | Leave blank to skip the gate |
| SpEL parse error | Engine rejects the entire lane (`false` returned) | Use the PricingMatrixBuilder UI instead of writing SpEL by hand |

---

## 8. Quick Reference Card

```
# CHECK: CIBIL thresholds
#cibilScore >= 750                          → high quality
#cibilScore >= 700 && #cibilScore <= 749    → mid range

# CHECK: Loan amount bands
#loanAmount <= 5000000                      → up to ₹50L
#loanAmount > 10000000                      → above ₹1Cr

# CHECK: Employment type
#employmentType == 'SALARIED'
#employmentType == 'SELF_EMPLOYED_PROFESSIONAL'
#employmentType == 'SELF_EMPLOYED_NON_PROFESSIONAL'

# CHECK: Existing EMI cap
#existingEmiTotal <= 50000

# CHECK: Property type
#propertyType == 'FLAT'
#propertyType == 'APARTMENT'

# COMBINED EXAMPLE: Bonus LTV for good profile
(#cibilScore >= 750 && #loanAmount <= 5000000) ? 85 : 80

# COMBINED EXAMPLE: Strict condition gate
SPEL: #cibilScore >= 700 && #existingEmiTotal < 40000 && #applicantAge <= 58
```

---

## 9. Who Should Write SpEL?

| Task | Tool to use |
|---|---|
| Tier-based LTV/FOIR by CIBIL or amount | **PricingMatrixBuilder UI** (auto-compiles SpEL) |
| Simple deny condition (e.g. max existing EMI) | **Conditions field** with `SPEL:` prefix |
| Complex policy deviation gate | **Deviation Formulae field** with `SPEL:` prefix |
| Deny specific employer/property types | **Negative lists** (comma-separated text, no SpEL needed) |

> For LTV and FOIR, always prefer the **visual Pricing Matrix Builder** over hand-writing SpEL. It validates inputs, prevents syntax errors, and shows a live preview of the compiled expression.
