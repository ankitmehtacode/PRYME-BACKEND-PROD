# PRYME Eligibility Engine — Test Profiles

> **Purpose**: Comprehensive test profiles for the client covering every employment type, all loan products, all surrogate income programs, and multiple banks simultaneously. Each profile includes reasoning for field values.

---

## System Reference

### Employment Types (Frontend → Backend mapping)
| Frontend Value | Backend Normalized | Description |
|---|---|---|
| `SALARIED` | `Salaried` | Private/Government salaried employees |
| `PROFESSIONAL` | `SEP/SENP` | CA, CS, Doctor, Lawyer |
| `SELF_EMPLOYED` | `SEP/SENP` | Business owners (ITR/GST/Banking/CashFlow) |

### Loan Types (Frontend → Backend mapping)
| Frontend Value | Backend Code | Available Banks |
|---|---|---|
| `HOME_LOAN` | `HL` | HDFC, SBI, ICICI, L&T, Bajaj, Bandhan, BOB, ABFL, Yes Bank, JIO, Tata Capital |
| `LOAN_AGAINST_PROPERTY` | `LAP` | HDFC, SBI, ICICI, L&T, Bajaj, Bandhan, BOB, ABFL, Yes Bank, JIO, Tata Capital, IDFC, IDBI |
| `BUSINESS_LOAN` | `BL` | (Limited coverage) |
| `PERSONAL_LOAN` | `PL` | (Limited coverage) |

### Surrogate Income Programs
| Program | Required Fields | Who Uses It |
|---|---|---|
| `NIP` | `pat`, `depreciation`, `interestExpense` | Salaried (salary as PAT), Self-Employed ITR-Based |
| `SENP` | `grossReceipts`, `profession` | Professionals (CA/CS/Doctor) |
| `SEP` | `grossReceipts`, `profession`, `lenderName`, `loanType` | Professionals (bank-specific multiplier) |
| `GST` | `gstrTurnover12Months`, `businessType` | Self-Employed GST-Based |
| `BANKING` | `averageBankBalance` or `bankBalanceSamples` | Self-Employed Banking Program |
| `CASHFLOW` | `averageBankBalance` or `bankBalanceSamples` | Self-Employed Cash Flow Program |
| `CPM_SEP` | `pat`, `depreciation`, `grossReceipts`, `profession`, `lenderName` | Professional hybrid (cap-based) |

### Property Types → Categories
| Sub-Type | Category |
|---|---|
| `FLAT`, `HOME`, `VILLA`, `APARTMENT`, `BUILDER_FLOOR`, `ROW_HOUSE`, `PENTHOUSE` | `RESIDENTIAL` |
| `HOSPITAL`, `HOSTEL`, `SHOP`, `WAREHOUSE`, `GODOWN`, `OFFICE`, `SCHOOL` | `COMMERCIAL` |
| `FACTORIES`, `WAREHOUSES`, `DISTRIBUTION_CENTER` | `INDUSTRIAL` |
| `PLOT` | `PLOT` |

---

## Profile 1 — Salaried, Home Loan, NIP (Multi-Bank Aggregator)

> **Scenario**: A 32-year-old salaried private sector employee wants to buy a flat in Indore. Good CIBIL score, stable income. This profile is designed to hit **maximum bank coverage** — should qualify across HDFC, SBI, ICICI, L&T, Bajaj, Bandhan, BOB, ABFL, Yes Bank.

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 32,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 3500000,
  "propertyValue": 5000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 85000,
  "existingEmiTotal": 5000,
  "businessAgeYears": 0,
  "workExpYears": 8,
  "idempotencyKey": "TEST-SAL-HL-001",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 95000,
  "pinCode": "452001",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 1020000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `lenderId` | `null` | **Aggregator mode** — evaluates across ALL active lenders |
| `loanType` | `HOME_LOAN` | Tests the primary HL product line |
| `cibilScore` | `750` | Sweet spot — clears most bank CIBIL floors (680-725) without being edge-case |
| `applicantAge` | `32` | Young enough for 20-year tenure without hitting maxAge (55-65) at maturity |
| `employmentType` | `SALARIED` | Maps to `Salaried` in DB; matches SALARIED_SEP and Salaried conditions |
| `propertyType` | `FLAT` | Resolves to `RESIDENTIAL` — universally accepted by all banks |
| `cityTier` | `TIER_1` | Indore is classified as Tier 1 in the engine |
| `loanAmount` | `₹35L` | Mid-range; within min/max loan amount for most products |
| `propertyValue` | `₹50L` | LTV = 35L/50L = 70% — comfortably within most banks' 75-85% LTV |
| `requestedTenureMonths` | `240` (20yr) | Long tenure = lower EMI = better FOIR clearance |
| `monthlyIncome` | `₹85K` | Decent salaried income; FOIR with ₹5K existing EMI will pass most 65% thresholds |
| `existingEmiTotal` | `₹5K` | Low existing obligations — maximizes disposable income for FOIR |
| `workExpYears` | `8` | Exceeds most banks' min 1-3yr requirement |
| `pinCode` | `452001` | Indore city center — passes geo-fence gate (452xxx) |
| `incomeComputationInput.programName` | `NIP` | Salaried uses NIP with salary annualized as PAT |
| `incomeComputationInput.pat` | `₹10.2L` | 85K × 12 = ₹10.2L annual salary as PAT |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹85,000 (from NIP: 10.2L / 12)
* **LTV**: 70.0% (35L / 50L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹32,354 / Income ₹85,000 = 38.1% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - lowest rate: 7.20%)
  - `HDFC-HL` (7.25%)
  - `SBI-HL` (7.60%)
  - `ICICI-HL` (7.60%)
  - `BANDHAN-HL` (8.00%)
  - `ABFL-HL` (8.20%)
  - `LT-HL` (8.20%)
  - `YES-HL` (8.05%)
  - `BAJAJ-HL` (9.25%)

---

## Profile 2 — Salaried, LAP (Loan Against Property)

> **Scenario**: A 45-year-old government employee wants LAP on an existing residential property. Tests the LAP product line across all banks.

```json
{
  "lenderId": null,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 720,
  "applicantAge": 45,
  "employmentType": "SALARIED",
  "propertyType": "HOME",
  "cityTier": "TIER_1",
  "loanAmount": 2500000,
  "propertyValue": 6000000,
  "requestedTenureMonths": 180,
  "monthlyIncome": 75000,
  "existingEmiTotal": 12000,
  "businessAgeYears": 0,
  "workExpYears": 20,
  "idempotencyKey": "TEST-SAL-LAP-002",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 85000,
  "pinCode": "452010",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 900000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `loanType` | `LOAN_AGAINST_PROPERTY` | Maps to `LAP` — tests LAP product line which includes extra banks (IDBI, IDFC) |
| `cibilScore` | `720` | Slightly lower than Profile 1 to test mid-tier CIBIL matching |
| `applicantAge` | `45` | Tests age-at-maturity check: 45 + 15yr = 60 (within most banks' 65 max) |
| `propertyType` | `HOME` | Resolves to `RESIDENTIAL` — universally accepted for LAP |
| `loanAmount` | `₹25L` | Conservative LTV: 25L/60L = 41.7% — well within LAP LTV limits (typically 50-65%) |
| `propertyValue` | `₹60L` | Higher property value supports LAP underwriting |
| `requestedTenureMonths` | `180` (15yr) | Standard LAP tenure |
| `existingEmiTotal` | `₹12K` | Higher existing EMI to test FOIR boundary |
| `workExpYears` | `20` | Senior employee — exceeds all work-exp requirements |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹75,000 (from NIP: 9.0L / 12)
* **LTV**: 41.7% (25L / 60L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹37,356 / Income ₹75,000 = 49.8% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `LT-LAP` (Recommended - lowest rate: 8.35%)
  - `HDFC-LAP` (9.20%)
  - `ICICI-LAP` (9.30%)
  - `SBI-LAP` (9.20%)
  - `BOB-LAP` (9.55%)
  - `YES-LAP` (9.60%)
  - `BAJAJ-LAP` (9.75%)
  - `ABFL-LAP` (10.05%)
  - `BANDHAN-LAP` (10.55%)

---

## Profile 3 — Professional (Doctor), Home Loan, SENP Program

> **Scenario**: A 38-year-old Doctor wants a home loan. Uses SENP surrogate income program. Tests `PROFESSIONAL` employment type mapping.

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 780,
  "applicantAge": 38,
  "employmentType": "PROFESSIONAL",
  "propertyType": "VILLA",
  "cityTier": "TIER_1",
  "loanAmount": 5000000,
  "propertyValue": 7500000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 150000,
  "existingEmiTotal": 8000,
  "businessAgeYears": 10,
  "workExpYears": 10,
  "idempotencyKey": "TEST-PROF-HL-003",
  "itrYearsAvailable": 3,
  "grossMonthlyIncome": 200000,
  "pinCode": "452003",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "SENP",
    "pat": null,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": 3600000,
    "profession": "Doctor",
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `employmentType` | `PROFESSIONAL` | Maps to `SEP/SENP` in backend — tests professional pathway |
| `propertyType` | `VILLA` | Resolves to `RESIDENTIAL` — tests a non-FLAT sub-type resolution |
| `cibilScore` | `780` | High CIBIL for premium products |
| `loanAmount` | `₹50L` | Higher ticket — tests higher loan amount brackets |
| `propertyValue` | `₹75L` | LTV = 66.7% — reasonable for professional profiles |
| `businessAgeYears` | `10` | Practice years — exceeds business vintage requirements |
| `workExpYears` | `10` | Same as practice years for professionals |
| `itrYearsAvailable` | `3` | Professionals typically file ITR; 3 years clears most banks' 2-3yr requirement |
| `grossMonthlyIncome` | `₹2L` | Declared gross income as fallback for minIncome check |
| `incomeComputationInput.programName` | `SENP` | Doctor uses SENP program (non-professional SEP multiplier) |
| `incomeComputationInput.grossReceipts` | `₹36L` | Annual gross receipts from medical practice |
| `incomeComputationInput.profession` | `Doctor` | Drives SENP multiplier: Doctor gets 2.5× (not 1.5× like CS) |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹750,000 (from SENP: 36L * 2.5 / 12)
* **LTV**: 66.7% (50L / 75L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹43,000 / Income ₹750,000 = 5.7% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - lowest rate: 7.15%)
  - `HDFC-HL` (7.20%)
  - `SBI-HL` (7.65%)
  - `ICICI-HL` (7.65%)
  - `BANDHAN-HL` (7.95%)
  - `YES-HL` (8.10%)
  - `ABFL-HL` (8.25%)
  - `LT-HL` (8.50%)
  - `BAJAJ-HL` (9.95%)

---

## Profile 4 — Professional (CA), LAP, SEP Program (Bank-Specific Multiplier)

> **Scenario**: A 42-year-old Chartered Accountant wants LAP. Tests SEP program with bank-specific multiplier lookup.

```json
{
  "lenderId": null,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 760,
  "applicantAge": 42,
  "employmentType": "PROFESSIONAL",
  "propertyType": "APARTMENT",
  "cityTier": "TIER_1",
  "loanAmount": 4000000,
  "propertyValue": 8000000,
  "requestedTenureMonths": 180,
  "monthlyIncome": 120000,
  "existingEmiTotal": 15000,
  "businessAgeYears": 15,
  "workExpYears": 15,
  "idempotencyKey": "TEST-PROF-LAP-004",
  "itrYearsAvailable": 3,
  "grossMonthlyIncome": 180000,
  "pinCode": "452009",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "SEP",
    "pat": null,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": 4200000,
    "profession": "CA",
    "lenderName": "L&T Finance",
    "loanType": "LAP"
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `employmentType` | `PROFESSIONAL` | CA is a professional — maps to `SEP/SENP` |
| `loanType` | `LOAN_AGAINST_PROPERTY` | LAP tests the second major product line |
| `propertyType` | `APARTMENT` | Another residential sub-type — tests `APARTMENT → RESIDENTIAL` mapping |
| `loanAmount` | `₹40L` | Higher LAP amount, but LTV = 50% (conservative) |
| `businessAgeYears` | `15` | Seasoned CA practice — exceeds all vintage requirements |
| `existingEmiTotal` | `₹15K` | Moderate existing EMI to test FOIR |
| `incomeComputationInput.programName` | `SEP` | Tests SEP program — requires `lenderName` and `loanType` for bank-specific multiplier |
| `incomeComputationInput.profession` | `CA` | CA gets different multiplier vs Doctor (CS=1.5×, others=2.5×, bank-specific for SEP) |
| `incomeComputationInput.lenderName` | `L&T Finance` | Triggers bank-specific SEP multiplier resolution |
| `incomeComputationInput.loanType` | `LAP` | JIO Finance has different CA multiplier per loan type (HL vs LAP) |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹875,000 (from SEP with L&T CA multiplier: 42L * 2.5 / 12)
* **LTV**: 50.0% (40L / 80L)
* **Effective FOIR**: 75% (special L&T SEP override)
* **FOIR Check**: PASS (Total EMI ₹55,000 / Income ₹875,000 = 6.3% vs 75% limit)
* **Expected Products in `/offers` Endpoint**:
  - `LT-LAP` (Recommended - lowest rate: 8.30%)
  - `HDFC-LAP` (8.50%)
  - `ICICI-LAP` (8.60%)
  - `SBI-LAP` (8.50%)
  - `BOB-LAP` (8.85%)
  - `YES-LAP` (8.90%)
  - `BAJAJ-LAP` (9.95%)
  - `ABFL-LAP` (10.10%)
  - `BANDHAN-LAP` (9.85%)

---

## Profile 5 — Self-Employed (ITR-Based / NIP), Home Loan

> **Scenario**: A 40-year-old business owner with ITR filing wants a home loan. Uses NIP program with real P&L data.

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 700,
  "applicantAge": 40,
  "employmentType": "SELF_EMPLOYED",
  "propertyType": "BUILDER_FLOOR",
  "cityTier": "TIER_1",
  "loanAmount": 4500000,
  "propertyValue": 7000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000,
  "existingEmiTotal": 10000,
  "businessAgeYears": 12,
  "workExpYears": 0,
  "idempotencyKey": "TEST-SE-HL-NIP-005",
  "itrYearsAvailable": 3,
  "grossMonthlyIncome": 130000,
  "pinCode": "452012",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 1500000,
    "depreciation": 200000,
    "interestExpense": 100000,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `employmentType` | `SELF_EMPLOYED` | Maps to `SEP/SENP` — tests self-employed pathway |
| `cibilScore` | `700` | Lower CIBIL to test mid-range products (some banks have 680 floor) |
| `propertyType` | `BUILDER_FLOOR` | Tests `BUILDER_FLOOR → RESIDENTIAL` mapping |
| `businessAgeYears` | `12` | Strong business vintage — exceeds most 3-5yr requirements |
| `workExpYears` | `0` | Self-employed don't have "work experience" — tests that engine doesn't fail on 0 |
| `itrYearsAvailable` | `3` | ITR-based program — 3 years is the gold standard |
| `incomeComputationInput.programName` | `NIP` | Net Income Program: PAT + Depreciation + Interest Expense |
| `incomeComputationInput.pat` | `₹15L` | Annual Profit After Tax from business |
| `incomeComputationInput.depreciation` | `₹2L` | Annual depreciation add-back |
| `incomeComputationInput.interestExpense` | `₹1L` | Interest expense add-back |

> **NIP Income Computation**: (PAT + Depreciation + Interest) / 12 = (15L + 2L + 1L) / 12 = **₹1.5L/month**

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹150,000 (from NIP: (15L PAT + 2L Depr + 1L Int) / 12)
* **LTV**: 64.3% (45L / 70L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹45,287 / Income ₹150,000 = 30.2% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - lowest rate: 7.90%)
  - `HDFC-HL` (7.95%)
  - `SBI-HL` (8.30%)
  - `ICICI-HL` (8.30%)
  - `LT-HL` (8.35%)
  - `BANDHAN-HL` (8.70%)
  - `YES-HL` (8.75%)
  - `ABFL-HL` (8.90%)
  - `BAJAJ-HL` (9.75%)

---

## Profile 6 — Self-Employed (GST-Based), LAP

> **Scenario**: A 35-year-old retailer wants LAP using GST returns as income proof. No ITR filing.

```json
{
  "lenderId": null,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 680,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "propertyType": "SHOP",
  "cityTier": "TIER_1",
  "loanAmount": 2000000,
  "propertyValue": 5000000,
  "requestedTenureMonths": 180,
  "monthlyIncome": 60000,
  "existingEmiTotal": 8000,
  "businessAgeYears": 5,
  "workExpYears": 0,
  "idempotencyKey": "TEST-SE-LAP-GST-006",
  "itrYearsAvailable": 0,
  "grossMonthlyIncome": 80000,
  "pinCode": "452018",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "GST",
    "pat": null,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": 7200000,
    "businessType": "Retail",
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `cibilScore` | `680` | Lower CIBIL — tests near-prime products (Bajaj Near Prime, etc.) |
| `propertyType` | `SHOP` | Resolves to `COMMERCIAL` — tests commercial property acceptance |
| `loanAmount` | `₹20L` | Conservative LAP amount; LTV = 40% |
| `itrYearsAvailable` | `0` | GST-based applicant typically doesn't file ITR |
| `incomeComputationInput.programName` | `GST` | Tests GST surrogate income program |
| `incomeComputationInput.gstrTurnover12Months` | `₹72L` | Last 12 months GSTR-3B turnover |
| `incomeComputationInput.businessType` | `Retail` | Drives GST multiplier: Service/Retail/Wholesale/Manufacturing have different rates |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹72,000 (from GST: 72L GSTR * 12% Retail Margin / 12)
* **LTV**: 40.0% (20L / 50L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹28,285 / Income ₹72,000 = 39.3% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `LT-LAP` (Recommended - lowest rate: 8.90%)
  - `BOB-LAP` (9.55%)
  - `YES-LAP` (9.60%)
  - `HDFC-LAP` (9.90%)
  - `ICICI-LAP` (10.00%)
  - `SBI-LAP` (9.90%)
  - `BAJAJ-LAP` (10.50%)
  - `ABFL-LAP` (11.50%)
  - `BANDHAN-LAP` (11.25%)

---

## Profile 7 — Self-Employed (Banking Program), Home Loan

> **Scenario**: A 30-year-old business owner with no ITR or GST, uses Average Bank Balance (ABB) as income proof.

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 730,
  "applicantAge": 30,
  "employmentType": "SELF_EMPLOYED",
  "propertyType": "ROW_HOUSE",
  "cityTier": "TIER_1",
  "loanAmount": 2000000,
  "propertyValue": 3500000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 50000,
  "existingEmiTotal": 0,
  "businessAgeYears": 4,
  "workExpYears": 0,
  "idempotencyKey": "TEST-SE-HL-BANK-007",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 50000,
  "pinCode": "453111",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "BANKING",
    "pat": null,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": 1000000,
    "bankBalanceSamples": [800000, 1200000, 950000, 1100000, 900000, 1050000, 1000000, 1150000],
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `propertyType` | `ROW_HOUSE` | Tests `ROW_HOUSE → RESIDENTIAL` mapping |
| `existingEmiTotal` | `0` | No existing obligations — cleanest FOIR scenario |
| `businessAgeYears` | `4` | Meets most banks' 3yr minimum |
| `pinCode` | `453111` | Indore **district** pincode (453xxx) — tests second geo-fence prefix |
| `incomeComputationInput.programName` | `BANKING` | Tests Banking Program surrogate |
| `incomeComputationInput.averageBankBalance` | `₹10L` | Pre-computed ABB as fallback |
| `incomeComputationInput.bankBalanceSamples` | `[8L, 12L, 9.5L, 11L, ...]` | Raw balance samples on 5th/10th/20th/25th of month — engine computes ABB from these |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹1,018,750 (average of bank balance samples)
* **LTV**: 57.1% (20L / 35L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹17,000 / Income ₹1,018,750 = 1.7% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - lowest rate: 7.70%)
  - `HDFC-HL` (7.85%)
  - `SBI-HL` (8.10%)
  - `ICICI-HL` (8.10%)
  - `BANDHAN-HL` (8.50%)
  - `ABFL-HL` (8.70%)
  - `LT-HL` (8.35%)
  - `YES-HL` (8.55%)
  - `BAJAJ-HL` (9.25%)

---

## Profile 8 — Self-Employed (Cash Flow Program), LAP

> **Scenario**: A 50-year-old business owner uses bank statement analysis (CashFlow) for LAP. Tests edge-case age at maturity.

```json
{
  "lenderId": null,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 710,
  "applicantAge": 50,
  "employmentType": "SELF_EMPLOYED",
  "propertyType": "WAREHOUSE",
  "cityTier": "TIER_1",
  "loanAmount": 3000000,
  "propertyValue": 8000000,
  "requestedTenureMonths": 120,
  "monthlyIncome": 80000,
  "existingEmiTotal": 20000,
  "businessAgeYears": 20,
  "workExpYears": 0,
  "idempotencyKey": "TEST-SE-LAP-CF-008",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 100000,
  "pinCode": "452020",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "CASHFLOW",
    "pat": null,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": 500000,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `applicantAge` | `50` | Tests age-at-maturity boundary: 50 + 10yr = 60 (some banks maxAge = 60-65) |
| `propertyType` | `WAREHOUSE` | Resolves to `COMMERCIAL` — tests commercial LAP |
| `requestedTenureMonths` | `120` (10yr) | Shorter tenure to keep age-at-maturity under 65 |
| `existingEmiTotal` | `₹20K` | High existing EMI — tests FOIR under pressure |
| `loanAmount/propertyValue` | `30L/80L` | Very low LTV (37.5%) — should pass all LTV checks |
| `incomeComputationInput.programName` | `CASHFLOW` | Tests CashFlow surrogate — bank statement analysis |
| `incomeComputationInput.averageBankBalance` | `₹5L` | Average monthly bank balance for cash flow assessment |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹500,000 (average monthly bank balance)
* **LTV**: 37.5% (30L / 80L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹58,003 / Income ₹500,000 = 11.6% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-LAP` (Recommended - lowest rate: 9.55%)
  - `HDFC-LAP` (9.20%)
  - `ICICI-LAP` (9.30%)
  - `SBI-LAP` (9.20%)
  - `YES-LAP` (9.60%)
  - `BAJAJ-LAP` (9.75%)
  - `ABFL-LAP` (10.85%)
  - `LT-LAP` (8.35%)
  - `BANDHAN-LAP` (10.55%)

---

## Profile 9 — Professional (CS), Home Loan, CPM_SEP Program

> **Scenario**: A 36-year-old Company Secretary uses the hybrid CPM_SEP program (cap-based computation). Tests the most complex income computation path.

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 770,
  "applicantAge": 36,
  "employmentType": "PROFESSIONAL",
  "propertyType": "PENTHOUSE",
  "cityTier": "TIER_1",
  "loanAmount": 6000000,
  "propertyValue": 10000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 180000,
  "existingEmiTotal": 5000,
  "businessAgeYears": 8,
  "workExpYears": 8,
  "idempotencyKey": "TEST-PROF-HL-CPM-009",
  "itrYearsAvailable": 3,
  "grossMonthlyIncome": 250000,
  "pinCode": "452005",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "CPM_SEP",
    "pat": 2000000,
    "depreciation": 300000,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": 5000000,
    "profession": "CS",
    "lenderName": "HDFC Bank",
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `propertyType` | `PENTHOUSE` | Tests `PENTHOUSE → RESIDENTIAL` mapping — premium property |
| `loanAmount` | `₹60L` | Higher ticket for professionals; LTV = 60% |
| `incomeComputationInput.programName` | `CPM_SEP` | Tests the most complex surrogate: uses PAT + Depreciation + Gross Receipts (capped) + Profession multiplier + Bank-specific lookup |
| `incomeComputationInput.profession` | `CS` | Company Secretary gets 1.5× multiplier (vs 2.5× for others) |
| `incomeComputationInput.lenderName` | `HDFC Bank` | Triggers HDFC-specific multiplier table |
| `incomeComputationInput.pat` | `₹20L` | PAT from CS practice |
| `incomeComputationInput.depreciation` | `₹3L` | Depreciation add-back |
| `incomeComputationInput.grossReceipts` | `₹50L` | Gross receipts (cap applied by engine) |

### Expected Results
* **Status**: Eligible
* **Computed Monthly Income**: ₹575,000 (from CPM: (20L PAT + 3L Depr) * 3 / 12)
* **LTV**: 60.0% (60L / 100L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹52,000 / Income ₹575,000 = 9.0% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - lowest rate: 7.15%)
  - `HDFC-HL` (7.20%)
  - `SBI-HL` (7.55%)
  - `ICICI-HL` (7.55%)
  - `BANDHAN-HL` (7.95%)
  - `YES-HL` (8.00%)
  - `ABFL-HL` (8.15%)
  - `LT-HL` (8.30%)
  - `BAJAJ-HL` (9.95%)

---

## Profile 10 — Geo-Fence Rejection Test (Non-Indore PIN)

> **Scenario**: Same strong profile as Profile 1, but pincode is outside Indore. **Expected result: REJECTION** with geo-fence error.

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 800,
  "applicantAge": 30,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 2500000,
  "propertyValue": 4000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000,
  "existingEmiTotal": 0,
  "businessAgeYears": 0,
  "workExpYears": 6,
  "idempotencyKey": "TEST-GEOFENCE-010",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 110000,
  "pinCode": "400001",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 1200000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `pinCode` | `400001` | **Mumbai pincode** — fails `452xxx/453xxx` geo-fence check |
| All other fields | Strong profile | Intentionally strong to prove geo-fence is the ONLY reason for rejection |

### Expected Results
* **Status**: Rejected (Pre-flight / Geo-fence fail)
* **Expected Products in `/offers` Endpoint**:
  - **None** (Fails Indore-only operations geo-fence check: Pincode `400001` is outside service region)
  - **Reason**: `"Service area restricted: PIN 400001 is outside Indore (452xxx/453xxx)"`

---

## Profile 11 — Low Loan Amount Filter (₹15L HL)

> **Scenario**: A 28-year-old salaried applicant wants a small Home Loan of ₹15L. Excellent CIBIL score (750). Because most prime lenders (like HDFC, Bajaj, ABFL) require a minimum loan amount of ₹35L (or ₹20L/₹21L for ICICI/Yes), they are rejected. **Expected result: Only SBI and Bandhan Bank match.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 28,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 1500000,
  "propertyValue": 2500000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 50000,
  "existingEmiTotal": 0,
  "businessAgeYears": 0,
  "workExpYears": 3,
  "idempotencyKey": "TEST-MIN-AMOUNT-011",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 55000,
  "pinCode": "452001",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 600000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `loanAmount` | `₹15L` | Targets the minimum loan amount filter of individual products |
| All other fields | Strong profile | Standard salaried profile to highlight loan amount exclusion |

### Expected Results
* **Status**: Eligible (with few offers)
* **Computed Monthly Income**: ₹50,000 (from NIP: 6.0L / 12)
* **LTV**: 60.0% (15L / 25L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹11,643 / Income ₹50,000 = 23.3% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `SBI-HL` (Recommended - lowest rate: 7.60% ; min loan amount ₹10L)
  - `BANDHAN-HL` (8.00% ; min loan amount ₹2L)
* **Expected Excluded Lenders (Reasons)**:
  - `ABFL-HL`, `BAJAJ-HL` (Rejected: min loan amount is ₹35L)
  - `YES-HL` (Rejected: min loan amount is ₹21L)
  - `ICICI-HL` (Rejected: min loan amount is ₹20L)

---

## Profile 12 — Low CIBIL Score Filter (660 CIBIL HL)

> **Scenario**: A 35-year-old salaried applicant wants a Home Loan but has a lower CIBIL score of 660. Most prime lenders require CIBIL >= 675 or 700. **Expected result: Only SBI HL matches (CIBIL floor 550).**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 660,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 4000000,
  "propertyValue": 6000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 90000,
  "existingEmiTotal": 5000,
  "businessAgeYears": 0,
  "workExpYears": 6,
  "idempotencyKey": "TEST-LOW-CIBIL-012",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 95000,
  "pinCode": "452002",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 1080000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `cibilScore` | `660` | Fails min CIBIL gates for most premium products (ABFL, Bajaj, Bandhan, Yes, ICICI) |
| All other fields | Strong profile | Standard salaried profile to highlight CIBIL score exclusions |

### Expected Results
* **Status**: Eligible (with single offer)
* **Computed Monthly Income**: ₹90,000 (from NIP: 10.8L / 12)
* **LTV**: 66.7% (40L / 60L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹36,254 / Income ₹90,000 = 40.3% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `SBI-HL` (Recommended - rate: 9.00% ; CIBIL floor is 550)
* **Expected Excluded Lenders (Reasons)**:
  - `ABFL-HL` (Rejected: min CIBIL is 675)
  - `BAJAJ-HL`, `YES-HL` (Rejected: min CIBIL is 680)
  - `BANDHAN-HL`, `ICICI-HL` (Rejected: min CIBIL is 700)

---

## Profile 13 — Age-at-Maturity Restriction (48 Year Old Salaried HL, 20yr Tenure)

> **Scenario**: A 48-year-old salaried applicant wants a 20-year (240 months) Home Loan. The maturity age is 68. Lenders like ABFL, Bajaj, and Bandhan have a maximum maturity age of 60 or 62 for salaried. **Expected result: ABFL, Bajaj, Bandhan, and BOB reject.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 770,
  "applicantAge": 48,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 4500000,
  "propertyValue": 7000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000,
  "existingEmiTotal": 5000,
  "businessAgeYears": 0,
  "workExpYears": 15,
  "idempotencyKey": "TEST-MATURITY-AGE-013",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 110000,
  "pinCode": "452003",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 1200000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `applicantAge` | `48` | With 20-year tenure, age-at-maturity is 68, which violates max age limit of multiple products |
| All other fields | Strong profile | Standard salaried profile to highlight age-at-maturity exclusions |

### Expected Results
* **Status**: Eligible (with subset of offers)
* **Computed Monthly Income**: ₹100,000 (from NIP: 1.2M / 12)
* **LTV**: 64.3% (45L / 70L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹40,287 / Income ₹100,000 = 40.3% vs 65% limit)
* **Expected Products in `/offers` Endpoint**:
  - `HDFC-HL` (Recommended - rate: 7.25% ; max age at maturity is 70)
  - `SBI-HL` (7.60% ; max age at maturity is 70)
  - `ICICI-HL` (7.60% ; max age at maturity is 70)
  - `YES-HL` (8.05% ; max age at maturity is 70)
* **Expected Excluded Lenders (Reasons)**:
  - `BANDHAN-HL`, `BOB-HL` (Rejected: max age at maturity is 60)
  - `ABFL-HL`, `BAJAJ-HL` (Rejected: max age at maturity is 62)

---

## Profile 14 — Low Income Restriction (₹28K Salaried HL)

> **Scenario**: A 30-year-old salaried applicant wants a Home Loan of ₹25L but has a lower monthly income of ₹28,000. Most prime lenders require a minimum monthly income of ₹30,000 or ₹40,000. **Expected result: Only BOB and Bandhan Bank match.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 30,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 2500000,
  "propertyValue": 4500000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 28000,
  "existingEmiTotal": 0,
  "businessAgeYears": 0,
  "workExpYears": 4,
  "idempotencyKey": "TEST-MIN-INCOME-014",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 32000,
  "pinCode": "452001",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 336000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `monthlyIncome` | `₹28K` | Under ₹30K threshold, filtering out most prime lenders |
| All other fields | Strong profile | Standard salaried profile to highlight the minimum income filter |

### Expected Results
* **Status**: Eligible (with restricted offers)
* **Computed Monthly Income**: ₹28,000 (from NIP: 336K / 12)
* **LTV**: 55.6% (25L / 45L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS (Total EMI ₹19,405 / Income ₹28,000 = 69.3% vs 65% limit check, passes proposed/total criteria)
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - lowest rate: 7.20% ; min income ₹10K)
  - `BANDHAN-HL` (8.00% ; min income ₹15K)
* **Expected Excluded Lenders (Reasons)**:
  - `HDFC-HL`, `YES-HL`, `JIO-HL`, `TATA-HL` (Rejected: min income is ₹40K)
  - `ABFL-HL`, `ICICI-HL`, `LT-HL`, `BAJAJ-HL`, `SBI-HL` (Rejected: min income is ₹30K)

---

## Profile 15 — Property Type Restriction (PLOT Home Loan)

> **Scenario**: A 32-year-old salaried applicant wants a Home Loan of ₹40L for buying a residential plot. Standard home loan products generally restrict property types to category `RESIDENTIAL` (flats, row houses) and reject category `PLOT` or explicitly list plot as negative property. **Expected result: Only Bajaj Prime, Yes Bank, Tata Capital, and ABFL match.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 760,
  "applicantAge": 32,
  "employmentType": "SALARIED",
  "propertyType": "PLOT",
  "cityTier": "TIER_1",
  "loanAmount": 4000000,
  "propertyValue": 7000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 85000,
  "existingEmiTotal": 5000,
  "businessAgeYears": 0,
  "workExpYears": 6,
  "idempotencyKey": "TEST-PROP-PLOT-015",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 95000,
  "pinCode": "452001",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 1020000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `propertyType` | `PLOT` | Resolves to property category `PLOT`, triggering property-type exclusions |
| All other fields | Strong profile | High CIBIL, high income, standard salaried profile |

### Expected Results
* **Status**: Eligible (with property-specific offers)
* **Computed Monthly Income**: ₹85,000 (from NIP: 1.02M / 12)
* **LTV**: 57.1% (40L / 70L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS
* **Expected Products in `/offers` Endpoint**:
  - `YES-HL` (8.05% ; allows PLOT category)
  - `BAJAJ-HL` (9.25% ; allows PLOT category)
  - `ABFL-HL` (8.20% ; allows PLOT category)
  - `TATA-HL` (8.50% ; allows PLOT category)
* **Expected Excluded Lenders (Reasons)**:
  - `LT-HL` (Rejected: plot is in negative property list)
  - `HDFC-HL` (Rejected: plot is in negative property list)
  - `SBI-HL`, `ICICI-HL`, `BANDHAN-HL`, `BOB-HL` (Rejected: standard HL lanes restrict property type to residential category FLAT/APARTMENT/HOME)

---

## Profile 16 — Combined Low CIBIL & Low Income (660 CIBIL & ₹32K Income)

> **Scenario**: A 35-year-old salaried applicant wants a Home Loan of ₹30L. They have a lower CIBIL of 660 and a lower monthly income of ₹32,000. **Expected result: Only SBI, BOB, and L&T Finance match.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 660,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 3000000,
  "propertyValue": 5000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 32000,
  "existingEmiTotal": 0,
  "businessAgeYears": 0,
  "workExpYears": 6,
  "idempotencyKey": "TEST-CIBIL-INCOME-016",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 35000,
  "pinCode": "452002",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 384000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `cibilScore` | `660` | Filters out high-CIBIL lenders (Yes, Bajaj, Bandhan, ICICI, ABFL require >= 675/680/700) |
| `monthlyIncome` | `₹32K` | Filters out high-income lenders (HDFC, Yes, Jio, Tata require >= 40K) |

### Expected Results
* **Status**: Eligible (highly restricted offers)
* **Computed Monthly Income**: ₹32,000 (from NIP: 384K / 12)
* **LTV**: 60.0% (30L / 50L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - rate: 8.50% ; CIBIL floor 650, min income 10K)
  - `LT-HL` (8.35% ; CIBIL floor 650, min income 30K)
  - `SBI-HL` (9.00% ; CIBIL floor 550, min income 30K)
* **Expected Excluded Lenders (Reasons)**:
  - `ABFL-HL` (Rejected: min CIBIL is 675)
  - `BAJAJ-HL`, `YES-HL` (Rejected: min CIBIL is 680)
  - `BANDHAN-HL`, `ICICI-HL` (Rejected: min CIBIL is 700)
  - `HDFC-HL`, `JIO-HL`, `TATA-HL` (Rejected: min income is ₹40K)

---

## Profile 17 — SE GST, Low Turnover and Short Business Vintage (2yr Vintage)

> **Scenario**: A 34-year-old business owner wants a Home Loan of ₹30L using their GST turnover (₹48L). They run a wholesale business (8% profit margin) and have been operating for 2 years. **Expected result: Only ICICI Bank matches.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 730,
  "applicantAge": 34,
  "employmentType": "SELF_EMPLOYED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 3000000,
  "propertyValue": 6000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 30000,
  "existingEmiTotal": 0,
  "businessAgeYears": 2,
  "workExpYears": 0,
  "idempotencyKey": "TEST-SE-GST-VINTAGE-017",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 45000,
  "pinCode": "452005",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "GST",
    "pat": null,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": 4800000,
    "businessType": "Wholesale",
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `businessAgeYears` | `2` | Excludes L&T, Bajaj, Bandhan, Yes, Jio, Tata (require >= 3 years) |
| `incomeComputationInput.businessType` | `Wholesale` | Drives Wholesale profit margin: 8% margin |
| `incomeComputationInput.gstrTurnover12Months` | `₹48L` | Computed monthly income: 48L * 8% / 12 = ₹32K. Excludes HDFC/JIO/YES/ABFL (require higher income) |

### Expected Results
* **Status**: Eligible (Single offer)
* **Computed Monthly Income**: ₹32,000 (from GST: 48L GSTR * 8% Wholesale Margin / 12)
* **LTV**: 50.0% (30L / 60L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS
* **Expected Products in `/offers` Endpoint**:
  - `ICICI-HL` (Recommended - rate: 8.10% ; min business vintage 2 years, min income 30K)
* **Expected Excluded Lenders (Reasons)**:
  - `LT-HL`, `BAJAJ-HL`, `BANDHAN-HL`, `YES-HL`, `JIO-HL`, `TATA-HL` (Rejected: require min business vintage of 3 years)
  - `HDFC-HL` (Rejected: requires min business vintage of 5 years for GST program)
  - `ABFL-HL` (Rejected: requires min business vintage of 3 years)

---

## Profile 18 — High Property Value, Low CIBIL with Low LTV Surrogate

> **Scenario**: A 30-year-old salaried applicant wants a Home Loan of ₹30L on a high-value property of ₹80L (LTV = 37.5%). They have a lower CIBIL of 660. While they fail standard CIBIL limits for ABFL (675 floor), they qualify under ABFL's specialized `LOW_LTV` surrogate lane (which permits CIBIL down to 650 if LTV is <= 50%). **Expected result: SBI, BOB, L&T, and ABFL match.**

```json
{
  "lenderId": null,
  "loanType": "HOME_LOAN",
  "cibilScore": 660,
  "applicantAge": 30,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "cityTier": "TIER_1",
  "loanAmount": 3000000,
  "propertyValue": 8000000,
  "requestedTenureMonths": 240,
  "monthlyIncome": 60000,
  "existingEmiTotal": 5000,
  "businessAgeYears": 0,
  "workExpYears": 5,
  "idempotencyKey": "TEST-LOW-LTV-SURROGATE-018",
  "itrYearsAvailable": null,
  "grossMonthlyIncome": 65000,
  "pinCode": "452003",
  "propertyCategory": null,
  "businessPropertyCategory": null,
  "incomeComputationInput": {
    "programName": "NIP",
    "pat": 720000,
    "depreciation": null,
    "interestExpense": null,
    "averageBankBalance": null,
    "bankBalanceSamples": null,
    "gstrTurnover12Months": null,
    "businessType": null,
    "grossReceipts": null,
    "profession": null,
    "lenderName": null,
    "loanType": null
  }
}
```

### Reasoning

| Field | Value | Why |
|---|---|---|
| `cibilScore` | `660` | Fails standard ABFL floor (675) but triggers fallback to LOW_LTV lane (allows CIBIL >= 650) |
| `propertyValue` | `₹80L` | Makes LTV = 30L / 80L = 37.5%, satisfying ABFL's Low LTV cap of <= 50% |

### Expected Results
* **Status**: Eligible (via Low LTV fallback)
* **Computed Monthly Income**: ₹60,000 (from NIP: 720K / 12)
* **LTV**: 37.5% (30L / 80L)
* **Effective FOIR**: 65% (default multiplier)
* **FOIR Check**: PASS
* **Expected Products in `/offers` Endpoint**:
  - `BOB-HL` (Recommended - rate: 8.50% ; CIBIL floor 650)
  - `LT-HL` (8.35% ; CIBIL floor 650)
  - `ABFL-HL` (8.20% ; qualifies via LOW_LTV fallback; CIBIL floor 650, LTV <= 50%)
  - `SBI-HL` (9.00% ; CIBIL floor 550)
* **Expected Excluded Lenders (Reasons)**:
  - `BAJAJ-HL`, `YES-HL` (Rejected: min CIBIL is 680)
  - `BANDHAN-HL`, `ICICI-HL` (Rejected: min CIBIL is 700)

---

## Coverage Matrix

| # | Employment | Loan | Program | Property | CIBIL | Banks Expected | Special Test |
|---|---|---|---|---|---|---|---|
| 1 | SALARIED | HL | NIP | FLAT (Res) | 750 | HDFC, SBI, ICICI, L&T, Bajaj, Bandhan, BOB, ABFL, Yes | Multi-bank aggregator |
| 2 | SALARIED | LAP | NIP | HOME (Res) | 720 | All LAP banks + IDBI, IDFC | Age-at-maturity, higher EMI |
| 3 | PROFESSIONAL | HL | SENP | VILLA (Res) | 780 | All HL banks (SEP/SENP lanes) | Doctor, high CIBIL |
| 4 | PROFESSIONAL | LAP | SEP | APARTMENT (Res) | 760 | All LAP banks | CA, bank-specific multiplier |
| 5 | SELF_EMPLOYED | HL | NIP | BUILDER_FLOOR (Res) | 700 | Mid-tier banks | ITR-based, full P&L |
| 6 | SELF_EMPLOYED | LAP | GST | SHOP (Commercial) | 680 | Near-prime banks | No ITR, GST-only |
| 7 | SELF_EMPLOYED | HL | BANKING | ROW_HOUSE (Res) | 730 | ABB-qualifying banks | Bank balance samples, 453xxx PIN |
| 8 | SELF_EMPLOYED | LAP | CASHFLOW | WAREHOUSE (Comm) | 710 | CashFlow banks | Age boundary, high EMI |
| 9 | PROFESSIONAL | HL | CPM_SEP | PENTHOUSE (Res) | 770 | HDFC (explicit) + others | CS, complex computation |
| 10 | SALARIED | HL | NIP | FLAT (Res) | 800 | **NONE — REJECTED** | Geo-fence (Mumbai PIN) |
| 11 | SALARIED | HL | NIP | FLAT (Res) | 750 | SBI, Bandhan | Minimum loan amount restriction |
| 12 | SALARIED | HL | NIP | FLAT (Res) | 660 | SBI | Low CIBIL score limit check |
| 13 | SALARIED | HL | NIP | FLAT (Res) | 770 | HDFC, SBI, ICICI, Yes | Age-at-maturity restriction |
| 14 | SALARIED | HL | NIP | FLAT (Res) | 750 | BOB, Bandhan | Minimum income threshold (₹28K) |
| 15 | SALARIED | HL | NIP | PLOT (Plot) | 760 | Bajaj, Yes, ABFL, Tata | Property type category exclusion (PLOT) |
| 16 | SALARIED | HL | NIP | FLAT (Res) | 660 | SBI, BOB, L&T | Combined low CIBIL (660) & income (₹32K) |
| 17 | SELF_EMPLOYED | HL | GST | FLAT (Res) | 730 | ICICI | Short business vintage (2yr) GST program |
| 18 | SALARIED | HL | NIP | FLAT (Res) | 660 | SBI, BOB, L&T, ABFL | LOW_LTV surrogate fallback (CIBIL 660, LTV 37.5%) |

### Fields Covered Across All Profiles

| Field | Profiles That Test It |
|---|---|
| `lenderId` (null = aggregator) | All profiles |
| `loanType` (HL) | 1, 3, 5, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 |
| `loanType` (LAP) | 2, 4, 6, 8 |
| `cibilScore` (660-800 range) | All profiles (660, 680, 700, 710, 720, 730, 750, 760, 770, 780, 800) |
| `applicantAge` (28-50 range) | All profiles |
| `employmentType` SALARIED | 1, 2, 10, 11, 12, 13, 14, 15, 16, 18 |
| `employmentType` PROFESSIONAL | 3, 4, 9 |
| `employmentType` SELF_EMPLOYED | 5, 6, 7, 8, 17 |
| `propertyType` (10 sub-types) | FLAT, HOME, VILLA, APARTMENT, BUILDER_FLOOR, SHOP, ROW_HOUSE, WAREHOUSE, PENTHOUSE, PLOT |
| `pinCode` (452xxx pass) | 1-6, 8-9, 11-18 |
| `pinCode` (453xxx pass) | 7 |
| `pinCode` (4xxxx fail) | 10 |
| `itrYearsAvailable` | 3, 4, 5, 9 (with value); 6 (0); others (null) |
| `grossMonthlyIncome` | All profiles |
| NIP program | 1, 2, 5, 10, 11, 12, 13, 14, 15, 16, 18 |
| SENP program | 3 |
| SEP program | 4 |
| GST program | 6, 17 |
| BANKING program | 7 |
| CASHFLOW program | 8 |
| CPM_SEP program | 9 |

---

> [!TIP]
> To test a **specific bank** instead of aggregator mode, set `lenderId` to the bank's ID from the `loan_products.lender_id` column. For example, `"lenderId": 1` for HDFC Bank (check your DB for exact IDs).

> [!IMPORTANT]
> All profiles use Indore pincodes (452xxx/453xxx) except Profile 10 which deliberately uses a Mumbai pincode to test the geo-fence rejection. PRYME currently operates **exclusively in Indore**.

