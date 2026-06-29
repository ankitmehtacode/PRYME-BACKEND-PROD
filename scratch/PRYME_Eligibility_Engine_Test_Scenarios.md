# PRYME Eligibility Engine Test Scenarios Suite

This document lists 105 structured scenarios to verify every validation logic and edge case in the engine.

## 1. Quick Reference Matrix

| ID | Verify Objective | Lender | Product | Employment | CIBIL | Income | Amount | Tenure | Age | Decision | Expected Logic / Reason |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-001 | Verify eligibility rules for CIBIL=550 with HDFC Bank | HDFC Bank | HOME_LOAN | SALARIED | 550 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **REJECTED** | CIBIL 550 is below HDFC HL floor of 650. |
| TC-002 | Verify eligibility rules for CIBIL=580 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 580 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **REJECTED** | CIBIL 580 is below L&T HL floor of 650. |
| TC-003 | Verify eligibility rules for CIBIL=600 with ICICI Bank | ICICI Bank | LOAN_AGAINST_PROPERTY | SALARIED | 600 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **REJECTED** | CIBIL 600 is below ICICI LAP floor of 650. |
| TC-004 | Verify eligibility rules for CIBIL=620 with Yes Bank | Yes Bank | HOME_LOAN | PROFESSIONAL | 620 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **REJECTED** | CIBIL 620 is below Yes Bank HL floor of 650. |
| TC-005 | Verify eligibility rules for CIBIL=640 with Bajaj Prime | Bajaj Prime | HOME_LOAN | SELF_EMPLOYED | 640 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **REJECTED** | CIBIL 640 is below Bajaj HL floor of 650. |
| TC-006 | Verify eligibility rules for CIBIL=650 with HDFC Bank | HDFC Bank | HOME_LOAN | SALARIED | 650 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 650 is exactly on the floor limit. Matches lowest ROI tier. |
| TC-007 | Verify eligibility rules for CIBIL=660 with Bandhan Bank | Bandhan Bank | HOME_LOAN | SALARIED | 660 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 660 is eligible for Bandhan Bank Home Loan. |
| TC-008 | Verify eligibility rules for CIBIL=680 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 680 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 680 is eligible. Standard ROI applies. |
| TC-009 | Verify eligibility rules for CIBIL=700 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 700 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 700 is eligible. Maps to mid-range ROI matrix. |
| TC-010 | Verify eligibility rules for CIBIL=720 with Bank of Baroda | Bank of Baroda | HOME_LOAN | PROFESSIONAL | 720 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 720 matches professional lane requirements. |
| TC-011 | Verify eligibility rules for CIBIL=740 with Yes Bank | Yes Bank | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 740 is eligible. High credit score profile. |
| TC-012 | Verify eligibility rules for CIBIL=750 with SBI | SBI | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 750 is eligible. Prime SBI Home Loan ROI applies. |
| TC-013 | Verify eligibility rules for CIBIL=780 with Aditya Birla Finance Limited | Aditya Birla Finance Limited | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 780 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 780 matches prime tier. Best ROI and LTV conditions. |
| TC-014 | Verify eligibility rules for CIBIL=800 with TATA Capital | TATA Capital | LOAN_AGAINST_PROPERTY | SALARIED | 800 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 800 is eligible. Super prime applicant status. |
| TC-015 | Verify eligibility rules for CIBIL=850 with Jio Finance | Jio Finance | HOME_LOAN | PROFESSIONAL | 850 | Net: ₹100,000 | ₹3,000,000 | 240 | 35 | **ELIGIBLE** | CIBIL 850 is eligible. Outstanding credit score profile. |
| TC-016 | Verify maturity logic for age=18 and tenure=120 months with HDFC Bank | HDFC Bank | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 120 | 18 | **REJECTED** | Age 18 is below the standard minimum entry age of 21. |
| TC-017 | Verify maturity logic for age=20 and tenure=120 months with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 120 | 20 | **REJECTED** | Age 20 is below L&T minimum entry age of 23. |
| TC-018 | Verify maturity logic for age=21 and tenure=180 months with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 180 | 21 | **ELIGIBLE** | Age 21 is exactly on the ICICI Bank HL floor age. |
| TC-019 | Verify maturity logic for age=23 and tenure=240 months with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 23 | **ELIGIBLE** | Age 23 is exactly on the L&T HL floor age. |
| TC-020 | Verify maturity logic for age=25 and tenure=240 months with Bank of Baroda | Bank of Baroda | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 25 | **ELIGIBLE** | Age 25 is eligible. Matches standard age limits. |
| TC-021 | Verify maturity logic for age=35 and tenure=360 months with SBI | SBI | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 360 | 35 | **ELIGIBLE** | Age 35 with 30 yr tenure. Maturity age is 65. Exactly on standard HL max limit. |
| TC-022 | Verify maturity logic for age=45 and tenure=240 months with Yes Bank | Yes Bank | HOME_LOAN | PROFESSIONAL | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 45 | **ELIGIBLE** | Age 45 with 20 yr tenure. Maturity age is 65. Safe limit. |
| TC-023 | Verify maturity logic for age=55 and tenure=120 months with HDFC Bank | HDFC Bank | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 120 | 55 | **ELIGIBLE** | Age 55 with 10 yr tenure. Maturity age is 65. Safe limit. |
| TC-024 | Verify maturity logic for age=58 and tenure=60 months with HDFC Bank | HDFC Bank | PERSONAL_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 60 | 58 | **REJECTED** | Age 58 + 5 yr tenure = 63. Exceeds PL max age at maturity limit (60). |
| TC-025 | Verify maturity logic for age=58 and tenure=240 months with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 58 | **REJECTED** | Age 58 + 20 yr tenure = 78. Exceeds L&T HL max age limit of 60 for salaried. |
| TC-026 | Verify maturity logic for age=59 and tenure=12 months with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 12 | 59 | **ELIGIBLE** | Age 59 + 1 yr tenure = 60. Exactly on the maximum age limit boundary for L&T salaried. |
| TC-027 | Verify maturity logic for age=61 and tenure=120 months with ICICI Bank | ICICI Bank | HOME_LOAN | SELF_EMPLOYED | 750 | Net: ₹100,000 | ₹2,000,000 | 120 | 61 | **ELIGIBLE** | Age 61 + 10 yr tenure = 71. Eligible since self-employed limit is 75 for ICICI. |
| TC-028 | Verify maturity logic for age=65 and tenure=120 months with Bank of Baroda | Bank of Baroda | HOME_LOAN | SELF_EMPLOYED | 750 | Net: ₹100,000 | ₹2,000,000 | 120 | 65 | **ELIGIBLE** | Age 65 + 10 yr tenure = 75. Exactly on BOB self-employed limit of 75. |
| TC-029 | Verify maturity logic for age=71 and tenure=60 months with Yes Bank | Yes Bank | HOME_LOAN | SELF_EMPLOYED | 750 | Net: ₹100,000 | ₹2,000,000 | 60 | 71 | **REJECTED** | Age 71 + 5 yr tenure = 76. Exceeds self-employed max maturity age (70). |
| TC-030 | Verify maturity logic for age=75 and tenure=12 months with SBI | SBI | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 12 | 75 | **REJECTED** | Age 75 exceeds all maximum age limits for home loans. |
| TC-031 | Verify minimum income floor checks for income=10000 with Bandhan Bank | Bandhan Bank | HOME_LOAN | SALARIED | 750 | Net: ₹10,000 | ₹1,000,000 | 180 | 35 | **REJECTED** | Income ₹10,000 is below Bandhan's minimum floor of ₹15,000. |
| TC-032 | Verify minimum income floor checks for income=14999 with Bandhan Bank | Bandhan Bank | HOME_LOAN | SALARIED | 750 | Net: ₹14,999 | ₹1,000,000 | 180 | 35 | **REJECTED** | Income ₹14,999 is below Bandhan's floor of ₹15,000. |
| TC-033 | Verify minimum income floor checks for income=15000 with Bandhan Bank | Bandhan Bank | HOME_LOAN | SALARIED | 750 | Net: ₹15,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹15,000 is exactly on Bandhan's minimum salary floor. |
| TC-034 | Verify minimum income floor checks for income=20000 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹20,000 | ₹1,000,000 | 180 | 35 | **REJECTED** | Income ₹20,000 is below ICICI NIP floor of ₹30,000. |
| TC-035 | Verify minimum income floor checks for income=24999 with L&T Finance | L&T Finance | HOME_LOAN | SELF_EMPLOYED | 750 | Net: ₹24,999 | ₹1,000,000 | 180 | 35 | **REJECTED** | Income ₹24,999 is below L&T Self-Employed NIP floor of ₹25,000. |
| TC-036 | Verify minimum income floor checks for income=25000 with L&T Finance | L&T Finance | HOME_LOAN | SELF_EMPLOYED | 750 | Net: ₹25,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹25,000 matches L&T Self-Employed floor exactly. |
| TC-037 | Verify minimum income floor checks for income=29999 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹29,999 | ₹1,000,000 | 180 | 35 | **REJECTED** | Income ₹29,999 is below L&T Salaried NIP floor of ₹30,000. |
| TC-038 | Verify minimum income floor checks for income=30000 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹30,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹30,000 matches L&T Salaried floor exactly. |
| TC-039 | Verify minimum income floor checks for income=35000 with TATA Capital | TATA Capital | LOAN_AGAINST_PROPERTY | SALARIED | 750 | Net: ₹35,000 | ₹1,000,000 | 180 | 35 | **REJECTED** | Income ₹35,000 is below Tata Capital floor of ₹40,000. |
| TC-040 | Verify minimum income floor checks for income=40000 with TATA Capital | TATA Capital | LOAN_AGAINST_PROPERTY | SALARIED | 750 | Net: ₹40,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹40,000 matches Tata Capital LAP floor exactly. |
| TC-041 | Verify minimum income floor checks for income=50000 with HDFC Bank | HDFC Bank | HOME_LOAN | SALARIED | 750 | Net: ₹50,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹50,000 is eligible. Standard NIP pricing applies. |
| TC-042 | Verify minimum income floor checks for income=80000 with SBI | SBI | HOME_LOAN | SALARIED | 750 | Net: ₹80,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹80,000 is eligible. Standard NIP pricing applies. |
| TC-043 | Verify minimum income floor checks for income=120000 with Yes Bank | Yes Bank | HOME_LOAN | PROFESSIONAL | 750 | Net: ₹120,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹120,000 is eligible. SEP pricing applies. |
| TC-044 | Verify minimum income floor checks for income=150000 with Aditya Birla Finance Limited | Aditya Birla Finance Limited | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 750 | Net: ₹150,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹150,000 is eligible for High Leverage ABFL program. |
| TC-045 | Verify minimum income floor checks for income=250000 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹250,000 | ₹1,000,000 | 180 | 35 | **ELIGIBLE** | Income ₹250,000 matches the highest FOIR bracket of L&T (80%). |
| TC-046 | Verify program routing for program=NIP and employment=SALARIED with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 740 | Program: NIP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Routes to standard monthly Net Income Program. FOIR=60%. |
| TC-047 | Verify program routing for program=SEP and employment=PROFESSIONAL with L&T Finance | L&T Finance | HOME_LOAN | PROFESSIONAL | 740 | Program: SEP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Routes to CA multiplier (2.5x gross receipts). FOIR=75%. |
| TC-048 | Verify program routing for program=SEP and employment=PROFESSIONAL with Yes Bank | Yes Bank | HOME_LOAN | PROFESSIONAL | 740 | Program: SEP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Routes to Doctor program. FOIR=80%. |
| TC-049 | Verify program routing for program=GST and employment=SELF_EMPLOYED with L&T Finance | L&T Finance | HOME_LOAN | SELF_EMPLOYED | 740 | Program: GST | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | GST Turnover program: 12% retail turnover is resolved as monthly income. FOIR=65%. |
| TC-050 | Verify program routing for program=Banking and employment=SELF_EMPLOYED with ICICI Bank | ICICI Bank | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Program: Banking | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Banking ABB program: Uses Average Bank Balance to resolve monthly income. FOIR=33%. |
| TC-051 | Verify program routing for program=CPM_SEP and employment=SELF_EMPLOYED with Yes Bank | Yes Bank | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Program: CPM_SEP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Cash Profit Method (PAT + Depreciation). FOIR=75%. |
| TC-052 | Verify program routing for program=NIP and employment=SELF_EMPLOYED with ICICI Bank | ICICI Bank | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Program: NIP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Dynamic NIP for self-employed: FOIR = 140 - LTV (1.40 - 0.60 = 0.80). |
| TC-053 | Verify program routing for program=SEP and employment=PROFESSIONAL with Jio Finance | Jio Finance | HOME_LOAN | PROFESSIONAL | 740 | Program: SEP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | CS Professional multiplier program (1.5x gross receipts). FOIR=70%. |
| TC-054 | Verify program routing for program=GST and employment=SELF_EMPLOYED with IDFC | IDFC | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Program: GST | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | IDFC GST Program with 75% FOIR matrix. |
| TC-055 | Verify program routing for program=Banking and employment=SELF_EMPLOYED with IDBI | IDBI | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Program: Banking | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | IDBI Banking ABB Program with 60% FOIR matrix. |
| TC-056 | Verify program routing for program=NIP and employment=SALARIED with Bank of Baroda | Bank of Baroda | HOME_LOAN | SALARIED | 740 | Program: NIP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | NIP Salaried routes to BOB standard matrix (60% to 75% FOIR). |
| TC-057 | Verify program routing for program=NIP and employment=SELF_EMPLOYED with Bank of Baroda | Bank of Baroda | HOME_LOAN | SELF_EMPLOYED | 740 | Program: NIP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | NIP Self Employed routes to BOB 70% FOIR lane, explicitly excluding professional 80% lane. |
| TC-058 | Verify program routing for program=NIP and employment=PROFESSIONAL with Bank of Baroda | Bank of Baroda | HOME_LOAN | PROFESSIONAL | 740 | Program: NIP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | NIP Professional routes to BOB 80% FOIR lane, excluding self-employed 70% lane. |
| TC-059 | Verify program routing for program=NIP and employment=SELF_EMPLOYED with Aditya Birla Finance Limited | Aditya Birla Finance Limited | LOAN_AGAINST_PROPERTY | SELF_EMPLOYED | 740 | Program: NIP | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | High leverage NIP Self-Employed, routes to 150% FOIR lane. |
| TC-060 | Verify program routing for program=Low LTV and employment=SALARIED with TATA Capital | TATA Capital | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Program: Low LTV | ₹3,000,000 | 180 | 35 | **ELIGIBLE** | Low LTV program: bypasses NIP/surrogate floor and caps LTV directly at 50%. |
| TC-061 | Verify property type rules for propertyType='FLAT' with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Standard Residential Flat is fully allowed. LTV resolved via HL grid. |
| TC-062 | Verify property type rules for propertyType='HOME' with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Residential Home is allowed. LTV resolved via HL grid. |
| TC-063 | Verify property type rules for propertyType='VILLA' with HDFC Bank | HDFC Bank | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Residential Villa is allowed. LTV resolved via HL grid. |
| TC-064 | Verify property type rules for propertyType='SHOP' with TATA Capital | TATA Capital | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Commercial Shop allowed for TATA Capital LAP with LTV capped at 60%. |
| TC-065 | Verify property type rules for propertyType='OFFICE' with ICICI Bank | ICICI Bank | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Commercial Office allowed for ICICI LAP with LTV capped at 60%. |
| TC-066 | Verify property type rules for propertyType='HOSPITAL' with Yes Bank | Yes Bank | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Commercial Hospital allowed for Yes Bank LAP with LTV capped at 60%. |
| TC-067 | Verify property type rules for propertyType='PLOT' with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Plot loan is allowed. LTV is capped at flat 70% per HL Plot rule. |
| TC-068 | Verify property type rules for propertyType='LAND' with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Land loan is allowed. LTV is capped at flat 70% per HL Plot rule. |
| TC-069 | Verify property type rules for propertyType='FACTORIES' with L&T Finance | L&T Finance | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | Industrial Factory allowed for L&T LAP with LTV capped at 50%. |
| TC-070 | Verify property type rules for propertyType='WAREHOUSE' with TATA Capital | TATA Capital | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | TATA Capital LAP allows Industrial Warehouses with LTV capped at 50%. |
| TC-071 | Verify property type rules for propertyType='GODOWN' with Bank of Baroda | Bank of Baroda | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **ELIGIBLE** | BOB LAP allows Industrial Godown with LTV capped at 50%. |
| TC-072 | Verify property type rules for propertyType='INFORMAL' with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **REJECTED** | Property sub-type matches negative property list (Informal is disallowed). |
| TC-073 | Verify property type rules for propertyType='Informal Property' with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **REJECTED** | Property description resolves to negative property (Informal is disallowed). |
| TC-074 | Verify property type rules for propertyType='SCHOOL' with L&T Finance | L&T Finance | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **REJECTED** | L&T Finance LAP explicitly denies School properties. |
| TC-075 | Verify property type rules for propertyType='HOTEL' with Bajaj Prime | Bajaj Prime | LOAN_AGAINST_PROPERTY | SALARIED | 740 | Net: ₹100,000 | ₹2,500,000 | 180 | 35 | **REJECTED** | Bajaj LAP explicitly denies Hotel properties. |
| TC-076 | Verify geo-fence validation for pinCode='452001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Indore City core. Fully inside service area. |
| TC-077 | Verify geo-fence validation for pinCode='452010' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Vijay Nagar Indore. Inside service area. |
| TC-078 | Verify geo-fence validation for pinCode='452020' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Bicholi Indore. Inside service area. |
| TC-079 | Verify geo-fence validation for pinCode='453001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Indore district rural. Inside service area. |
| TC-080 | Verify geo-fence validation for pinCode='453441' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Mhow Indore district. Inside service area. |
| TC-081 | Verify geo-fence validation for pinCode='453551' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Sanwer Indore district. Inside service area. |
| TC-082 | Verify geo-fence validation for pinCode='452111' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Indore city peripheral. Inside service area. |
| TC-083 | Verify geo-fence validation for pinCode='453115' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **ELIGIBLE** | Indore district rural. Inside service area. |
| TC-084 | Verify geo-fence validation for pinCode='462001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | Bhopal PIN. Outside PRYME Indore service boundary. |
| TC-085 | Verify geo-fence validation for pinCode='400001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | Mumbai PIN. Outside PRYME Indore service boundary. |
| TC-086 | Verify geo-fence validation for pinCode='110001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | New Delhi PIN. Outside PRYME Indore service boundary. |
| TC-087 | Verify geo-fence validation for pinCode='560001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | Bengaluru PIN. Outside PRYME Indore service boundary. |
| TC-088 | Verify geo-fence validation for pinCode='380001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | Ahmedabad PIN. Outside PRYME Indore service boundary. |
| TC-089 | Verify geo-fence validation for pinCode='45200' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | Malformed PIN (5 digits). Must fail validation. |
| TC-090 | Verify geo-fence validation for pinCode='4520001' | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹100,000 | ₹2,000,000 | 240 | 35 | **REJECTED** | Malformed PIN (7 digits). Must fail validation. |
| TC-091 | Verify edge cases for input=60000.01 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹60,000.01 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 60% FOIR (matches >= 60001.00 correctly due to .01 boundary check). |
| TC-092 | Verify edge cases for input=60000.00 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹60,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 50% FOIR (matches <= 60000.00 correctly. Decimals verified). |
| TC-093 | Verify edge cases for input=30000.00 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹30,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 50% FOIR (exactly on the lower salary boundary). |
| TC-094 | Verify edge cases for input=29999.99 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹29,999.99 | ₹2,500,000.0 | 240 | 35 | **REJECTED** | Below lowest salary floor of ₹30,000 (rejected on boundary check). |
| TC-095 | Verify edge cases for input=100000.01 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹100,000.01 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 65% FOIR (matches >= 100,001 slab accurately). |
| TC-096 | Verify edge cases for input=100000.00 with ICICI Bank | ICICI Bank | HOME_LOAN | SALARIED | 750 | Net: ₹100,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 60% FOIR (matches <= 100,000 slab accurately). |
| TC-097 | Verify edge cases for input=150000.00 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹150,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 75% FOIR (matches <= 150000 bracket accurately). |
| TC-098 | Verify edge cases for input=150000.01 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹150,000.01 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 80% FOIR (matches >= 150001 'No Limit' bracket accurately). |
| TC-099 | Verify edge cases for input=250000.00 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹250,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 80% FOIR (matches 'No Limit' upper bound slab). |
| TC-100 | Verify edge cases for input=3000000.00 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹3,000,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 90% LTV (requested loan amount exactly 30L is capped at 90% in L&T HL). |
| TC-101 | Verify edge cases for input=3000000.50 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹3,000,000.5 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 80% LTV (requested loan amount slightly over 30L is capped at 80% in L&T HL). |
| TC-102 | Verify edge cases for input=7500000.00 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹7,500,000.0 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 80% LTV (requested loan amount exactly 75L is capped at 80% in L&T HL). |
| TC-103 | Verify edge cases for input=7500000.50 with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Net: ₹7,500,000.5 | ₹2,500,000.0 | 240 | 35 | **ELIGIBLE** | 75% LTV (requested loan amount slightly over 75L is capped at 75% in L&T HL). |
| TC-104 | Verify edge cases for input=Cash with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Mode: Cash | ₹2,500,000.0 | 240 | 35 | **REJECTED** | Salary mode check: 'Cash' is explicitly disallowed for L&T Home Loans. |
| TC-105 | Verify edge cases for input=UPI with L&T Finance | L&T Finance | HOME_LOAN | SALARIED | 750 | Mode: UPI | ₹2,500,000.0 | 240 | 35 | **REJECTED** | Salary mode check: 'UPI' is explicitly disallowed for L&T Home Loans. |


## 2. Copy-Pasteable JSON Payloads

Below are the JSON request bodies mapped by Scenario ID:

### Scenario TC-001
**Objective**: Verify eligibility rules for CIBIL=550 with HDFC Bank  
**Expected Decision**: REJECTED (CIBIL 550 is below HDFC HL floor of 650.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 550,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-001",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-002
**Objective**: Verify eligibility rules for CIBIL=580 with L&T Finance  
**Expected Decision**: REJECTED (CIBIL 580 is below L&T HL floor of 650.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 580,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-002",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-003
**Objective**: Verify eligibility rules for CIBIL=600 with ICICI Bank  
**Expected Decision**: REJECTED (CIBIL 600 is below ICICI LAP floor of 650.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 600,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-003",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-004
**Objective**: Verify eligibility rules for CIBIL=620 with Yes Bank  
**Expected Decision**: REJECTED (CIBIL 620 is below Yes Bank HL floor of 650.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 620,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-004",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-005
**Objective**: Verify eligibility rules for CIBIL=640 with Bajaj Prime  
**Expected Decision**: REJECTED (CIBIL 640 is below Bajaj HL floor of 650.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 640,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-005",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-006
**Objective**: Verify eligibility rules for CIBIL=650 with HDFC Bank  
**Expected Decision**: ELIGIBLE (CIBIL 650 is exactly on the floor limit. Matches lowest ROI tier.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 650,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-006",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-007
**Objective**: Verify eligibility rules for CIBIL=660 with Bandhan Bank  
**Expected Decision**: ELIGIBLE (CIBIL 660 is eligible for Bandhan Bank Home Loan.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 660,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-007",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-008
**Objective**: Verify eligibility rules for CIBIL=680 with L&T Finance  
**Expected Decision**: ELIGIBLE (CIBIL 680 is eligible. Standard ROI applies.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 680,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-008",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-009
**Objective**: Verify eligibility rules for CIBIL=700 with ICICI Bank  
**Expected Decision**: ELIGIBLE (CIBIL 700 is eligible. Maps to mid-range ROI matrix.)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 700,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-009",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-010
**Objective**: Verify eligibility rules for CIBIL=720 with Bank of Baroda  
**Expected Decision**: ELIGIBLE (CIBIL 720 matches professional lane requirements.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 720,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-010",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-011
**Objective**: Verify eligibility rules for CIBIL=740 with Yes Bank  
**Expected Decision**: ELIGIBLE (CIBIL 740 is eligible. High credit score profile.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-011",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-012
**Objective**: Verify eligibility rules for CIBIL=750 with SBI  
**Expected Decision**: ELIGIBLE (CIBIL 750 is eligible. Prime SBI Home Loan ROI applies.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-012",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-013
**Objective**: Verify eligibility rules for CIBIL=780 with Aditya Birla Finance Limited  
**Expected Decision**: ELIGIBLE (CIBIL 780 matches prime tier. Best ROI and LTV conditions.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 780,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-013",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-014
**Objective**: Verify eligibility rules for CIBIL=800 with TATA Capital  
**Expected Decision**: ELIGIBLE (CIBIL 800 is eligible. Super prime applicant status.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 800,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-014",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-015
**Objective**: Verify eligibility rules for CIBIL=850 with Jio Finance  
**Expected Decision**: ELIGIBLE (CIBIL 850 is eligible. Outstanding credit score profile.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 850,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-cibil-015",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-016
**Objective**: Verify maturity logic for age=18 and tenure=120 months with HDFC Bank  
**Expected Decision**: REJECTED (Age 18 is below the standard minimum entry age of 21.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 18,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 120,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-016",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-017
**Objective**: Verify maturity logic for age=20 and tenure=120 months with L&T Finance  
**Expected Decision**: REJECTED (Age 20 is below L&T minimum entry age of 23.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 20,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 120,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-017",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-018
**Objective**: Verify maturity logic for age=21 and tenure=180 months with ICICI Bank  
**Expected Decision**: ELIGIBLE (Age 21 is exactly on the ICICI Bank HL floor age.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 21,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-018",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-019
**Objective**: Verify maturity logic for age=23 and tenure=240 months with L&T Finance  
**Expected Decision**: ELIGIBLE (Age 23 is exactly on the L&T HL floor age.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 23,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-019",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-020
**Objective**: Verify maturity logic for age=25 and tenure=240 months with Bank of Baroda  
**Expected Decision**: ELIGIBLE (Age 25 is eligible. Matches standard age limits.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 25,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-020",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-021
**Objective**: Verify maturity logic for age=35 and tenure=360 months with SBI  
**Expected Decision**: ELIGIBLE (Age 35 with 30 yr tenure. Maturity age is 65. Exactly on standard HL max limit.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 360,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-021",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-022
**Objective**: Verify maturity logic for age=45 and tenure=240 months with Yes Bank  
**Expected Decision**: ELIGIBLE (Age 45 with 20 yr tenure. Maturity age is 65. Safe limit.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 45,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-022",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-023
**Objective**: Verify maturity logic for age=55 and tenure=120 months with HDFC Bank  
**Expected Decision**: ELIGIBLE (Age 55 with 10 yr tenure. Maturity age is 65. Safe limit.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 55,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 120,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-023",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-024
**Objective**: Verify maturity logic for age=58 and tenure=60 months with HDFC Bank  
**Expected Decision**: REJECTED (Age 58 + 5 yr tenure = 63. Exceeds PL max age at maturity limit (60).)  

```json
{
  "lenderId": 105,
  "loanType": "PERSONAL_LOAN",
  "cibilScore": 750,
  "applicantAge": 58,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 60,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-024",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-025
**Objective**: Verify maturity logic for age=58 and tenure=240 months with L&T Finance  
**Expected Decision**: REJECTED (Age 58 + 20 yr tenure = 78. Exceeds L&T HL max age limit of 60 for salaried.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 58,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-025",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-026
**Objective**: Verify maturity logic for age=59 and tenure=12 months with L&T Finance  
**Expected Decision**: ELIGIBLE (Age 59 + 1 yr tenure = 60. Exactly on the maximum age limit boundary for L&T salaried.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 59,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 12,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-026",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-027
**Objective**: Verify maturity logic for age=61 and tenure=120 months with ICICI Bank  
**Expected Decision**: ELIGIBLE (Age 61 + 10 yr tenure = 71. Eligible since self-employed limit is 75 for ICICI.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 61,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 120,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-027",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-028
**Objective**: Verify maturity logic for age=65 and tenure=120 months with Bank of Baroda  
**Expected Decision**: ELIGIBLE (Age 65 + 10 yr tenure = 75. Exactly on BOB self-employed limit of 75.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 65,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 120,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-028",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-029
**Objective**: Verify maturity logic for age=71 and tenure=60 months with Yes Bank  
**Expected Decision**: REJECTED (Age 71 + 5 yr tenure = 76. Exceeds self-employed max maturity age (70).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 71,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 60,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-029",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-030
**Objective**: Verify maturity logic for age=75 and tenure=12 months with SBI  
**Expected Decision**: REJECTED (Age 75 exceeds all maximum age limits for home loans.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 75,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 3000000.0,
  "requestedTenureMonths": 12,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-age-030",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-031
**Objective**: Verify minimum income floor checks for income=10000 with Bandhan Bank  
**Expected Decision**: REJECTED (Income ₹10,000 is below Bandhan's minimum floor of ₹15,000.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 10000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-031",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-032
**Objective**: Verify minimum income floor checks for income=14999 with Bandhan Bank  
**Expected Decision**: REJECTED (Income ₹14,999 is below Bandhan's floor of ₹15,000.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 14999.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-032",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-033
**Objective**: Verify minimum income floor checks for income=15000 with Bandhan Bank  
**Expected Decision**: ELIGIBLE (Income ₹15,000 is exactly on Bandhan's minimum salary floor.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 15000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-033",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-034
**Objective**: Verify minimum income floor checks for income=20000 with ICICI Bank  
**Expected Decision**: REJECTED (Income ₹20,000 is below ICICI NIP floor of ₹30,000.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 20000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-034",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-035
**Objective**: Verify minimum income floor checks for income=24999 with L&T Finance  
**Expected Decision**: REJECTED (Income ₹24,999 is below L&T Self-Employed NIP floor of ₹25,000.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 24999.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-035",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-036
**Objective**: Verify minimum income floor checks for income=25000 with L&T Finance  
**Expected Decision**: ELIGIBLE (Income ₹25,000 matches L&T Self-Employed floor exactly.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 25000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-036",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-037
**Objective**: Verify minimum income floor checks for income=29999 with L&T Finance  
**Expected Decision**: REJECTED (Income ₹29,999 is below L&T Salaried NIP floor of ₹30,000.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 29999.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-037",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-038
**Objective**: Verify minimum income floor checks for income=30000 with L&T Finance  
**Expected Decision**: ELIGIBLE (Income ₹30,000 matches L&T Salaried floor exactly.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 30000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-038",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-039
**Objective**: Verify minimum income floor checks for income=35000 with TATA Capital  
**Expected Decision**: REJECTED (Income ₹35,000 is below Tata Capital floor of ₹40,000.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 35000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-039",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-040
**Objective**: Verify minimum income floor checks for income=40000 with TATA Capital  
**Expected Decision**: ELIGIBLE (Income ₹40,000 matches Tata Capital LAP floor exactly.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 40000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-040",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-041
**Objective**: Verify minimum income floor checks for income=50000 with HDFC Bank  
**Expected Decision**: ELIGIBLE (Income ₹50,000 is eligible. Standard NIP pricing applies.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 50000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-041",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-042
**Objective**: Verify minimum income floor checks for income=80000 with SBI  
**Expected Decision**: ELIGIBLE (Income ₹80,000 is eligible. Standard NIP pricing applies.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 80000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-042",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-043
**Objective**: Verify minimum income floor checks for income=120000 with Yes Bank  
**Expected Decision**: ELIGIBLE (Income ₹120,000 is eligible. SEP pricing applies.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 120000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-043",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-044
**Objective**: Verify minimum income floor checks for income=150000 with Aditya Birla Finance Limited  
**Expected Decision**: ELIGIBLE (Income ₹150,000 is eligible for High Leverage ABFL program.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 150000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-044",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-045
**Objective**: Verify minimum income floor checks for income=250000 with L&T Finance  
**Expected Decision**: ELIGIBLE (Income ₹250,000 matches the highest FOIR bracket of L&T (80%).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 1000000.0,
  "propertyValue": 1500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 250000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-income-045",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-046
**Objective**: Verify program routing for program=NIP and employment=SALARIED with L&T Finance  
**Expected Decision**: ELIGIBLE (Routes to standard monthly Net Income Program. FOIR=60%.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-046",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-047
**Objective**: Verify program routing for program=SEP and employment=PROFESSIONAL with L&T Finance  
**Expected Decision**: ELIGIBLE (Routes to CA multiplier (2.5x gross receipts). FOIR=75%.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-047",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "SEP",
    "grossReceipts": 3000000.0,
    "profession": "CA",
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-048
**Objective**: Verify program routing for program=SEP and employment=PROFESSIONAL with Yes Bank  
**Expected Decision**: ELIGIBLE (Routes to Doctor program. FOIR=80%.)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-048",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "SEP",
    "grossReceipts": 3000000.0,
    "profession": "CA",
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-049
**Objective**: Verify program routing for program=GST and employment=SELF_EMPLOYED with L&T Finance  
**Expected Decision**: ELIGIBLE (GST Turnover program: 12% retail turnover is resolved as monthly income. FOIR=65%.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-049",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "GST",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": 6000000.0,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-050
**Objective**: Verify program routing for program=Banking and employment=SELF_EMPLOYED with ICICI Bank  
**Expected Decision**: ELIGIBLE (Banking ABB program: Uses Average Bank Balance to resolve monthly income. FOIR=33%.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-050",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "Banking",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": 80000.0,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-051
**Objective**: Verify program routing for program=CPM_SEP and employment=SELF_EMPLOYED with Yes Bank  
**Expected Decision**: ELIGIBLE (Cash Profit Method (PAT + Depreciation). FOIR=75%.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-051",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "CPM_SEP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-052
**Objective**: Verify program routing for program=NIP and employment=SELF_EMPLOYED with ICICI Bank  
**Expected Decision**: ELIGIBLE (Dynamic NIP for self-employed: FOIR = 140 - LTV (1.40 - 0.60 = 0.80).)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-052",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-053
**Objective**: Verify program routing for program=SEP and employment=PROFESSIONAL with Jio Finance  
**Expected Decision**: ELIGIBLE (CS Professional multiplier program (1.5x gross receipts). FOIR=70%.)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-053",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "SEP",
    "grossReceipts": 3000000.0,
    "profession": "CA",
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-054
**Objective**: Verify program routing for program=GST and employment=SELF_EMPLOYED with IDFC  
**Expected Decision**: ELIGIBLE (IDFC GST Program with 75% FOIR matrix.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-054",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "GST",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": 6000000.0,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-055
**Objective**: Verify program routing for program=Banking and employment=SELF_EMPLOYED with IDBI  
**Expected Decision**: ELIGIBLE (IDBI Banking ABB Program with 60% FOIR matrix.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-055",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "Banking",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": 80000.0,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-056
**Objective**: Verify program routing for program=NIP and employment=SALARIED with Bank of Baroda  
**Expected Decision**: ELIGIBLE (NIP Salaried routes to BOB standard matrix (60% to 75% FOIR).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-056",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-057
**Objective**: Verify program routing for program=NIP and employment=SELF_EMPLOYED with Bank of Baroda  
**Expected Decision**: ELIGIBLE (NIP Self Employed routes to BOB 70% FOIR lane, explicitly excluding professional 80% lane.)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-057",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-058
**Objective**: Verify program routing for program=NIP and employment=PROFESSIONAL with Bank of Baroda  
**Expected Decision**: ELIGIBLE (NIP Professional routes to BOB 80% FOIR lane, excluding self-employed 70% lane.)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "PROFESSIONAL",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-058",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-059
**Objective**: Verify program routing for program=NIP and employment=SELF_EMPLOYED with Aditya Birla Finance Limited  
**Expected Decision**: ELIGIBLE (High leverage NIP Self-Employed, routes to 150% FOIR lane.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SELF_EMPLOYED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-059",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-060
**Objective**: Verify program routing for program=Low LTV and employment=SALARIED with TATA Capital  
**Expected Decision**: ELIGIBLE (Low LTV program: bypasses NIP/surrogate floor and caps LTV directly at 50%.)  

```json
{
  "lenderId": 200,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 3000000.0,
  "propertyValue": 4500000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-routing-060",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "Low LTV",
    "grossReceipts": 3000000.0,
    "profession": null,
    "gstrTurnover12Months": null,
    "averageBankBalance": null,
    "pat": 1200000.0,
    "depreciation": 100000.0
  }
}
```

---

### Scenario TC-061
**Objective**: Verify property type rules for propertyType='FLAT' with L&T Finance  
**Expected Decision**: ELIGIBLE (Standard Residential Flat is fully allowed. LTV resolved via HL grid.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "FLAT",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-061",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-062
**Objective**: Verify property type rules for propertyType='HOME' with ICICI Bank  
**Expected Decision**: ELIGIBLE (Residential Home is allowed. LTV resolved via HL grid.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "HOME",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-062",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-063
**Objective**: Verify property type rules for propertyType='VILLA' with HDFC Bank  
**Expected Decision**: ELIGIBLE (Residential Villa is allowed. LTV resolved via HL grid.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "VILLA",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-063",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-064
**Objective**: Verify property type rules for propertyType='SHOP' with TATA Capital  
**Expected Decision**: ELIGIBLE (Commercial Shop allowed for TATA Capital LAP with LTV capped at 60%.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "SHOP",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-064",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-065
**Objective**: Verify property type rules for propertyType='OFFICE' with ICICI Bank  
**Expected Decision**: ELIGIBLE (Commercial Office allowed for ICICI LAP with LTV capped at 60%.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "OFFICE",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-065",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-066
**Objective**: Verify property type rules for propertyType='HOSPITAL' with Yes Bank  
**Expected Decision**: ELIGIBLE (Commercial Hospital allowed for Yes Bank LAP with LTV capped at 60%.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "HOSPITAL",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-066",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-067
**Objective**: Verify property type rules for propertyType='PLOT' with L&T Finance  
**Expected Decision**: ELIGIBLE (Plot loan is allowed. LTV is capped at flat 70% per HL Plot rule.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "PLOT",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-067",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-068
**Objective**: Verify property type rules for propertyType='LAND' with ICICI Bank  
**Expected Decision**: ELIGIBLE (Land loan is allowed. LTV is capped at flat 70% per HL Plot rule.)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "LAND",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-068",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-069
**Objective**: Verify property type rules for propertyType='FACTORIES' with L&T Finance  
**Expected Decision**: ELIGIBLE (Industrial Factory allowed for L&T LAP with LTV capped at 50%.)  

```json
{
  "lenderId": 73,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "FACTORIES",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-069",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-070
**Objective**: Verify property type rules for propertyType='WAREHOUSE' with TATA Capital  
**Expected Decision**: ELIGIBLE (TATA Capital LAP allows Industrial Warehouses with LTV capped at 50%.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "WAREHOUSE",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-070",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-071
**Objective**: Verify property type rules for propertyType='GODOWN' with Bank of Baroda  
**Expected Decision**: ELIGIBLE (BOB LAP allows Industrial Godown with LTV capped at 50%.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "GODOWN",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-071",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-072
**Objective**: Verify property type rules for propertyType='INFORMAL' with L&T Finance  
**Expected Decision**: REJECTED (Property sub-type matches negative property list (Informal is disallowed).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "INFORMAL",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-072",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-073
**Objective**: Verify property type rules for propertyType='Informal Property' with ICICI Bank  
**Expected Decision**: REJECTED (Property description resolves to negative property (Informal is disallowed).)  

```json
{
  "lenderId": 105,
  "loanType": "HOME_LOAN",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "Informal Property",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-073",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-074
**Objective**: Verify property type rules for propertyType='SCHOOL' with L&T Finance  
**Expected Decision**: REJECTED (L&T Finance LAP explicitly denies School properties.)  

```json
{
  "lenderId": 73,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "SCHOOL",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-074",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-075
**Objective**: Verify property type rules for propertyType='HOTEL' with Bajaj Prime  
**Expected Decision**: REJECTED (Bajaj LAP explicitly denies Hotel properties.)  

```json
{
  "lenderId": 105,
  "loanType": "LOAN_AGAINST_PROPERTY",
  "cibilScore": 740,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "propertyType": "HOTEL",
  "loanAmount": 2500000.0,
  "propertyValue": 4000000.0,
  "requestedTenureMonths": 180,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-property-075",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-076
**Objective**: Verify geo-fence validation for pinCode='452001'  
**Expected Decision**: ELIGIBLE (Indore City core. Fully inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-076",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-077
**Objective**: Verify geo-fence validation for pinCode='452010'  
**Expected Decision**: ELIGIBLE (Vijay Nagar Indore. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-077",
  "pinCode": "452010",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-078
**Objective**: Verify geo-fence validation for pinCode='452020'  
**Expected Decision**: ELIGIBLE (Bicholi Indore. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-078",
  "pinCode": "452020",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-079
**Objective**: Verify geo-fence validation for pinCode='453001'  
**Expected Decision**: ELIGIBLE (Indore district rural. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-079",
  "pinCode": "453001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-080
**Objective**: Verify geo-fence validation for pinCode='453441'  
**Expected Decision**: ELIGIBLE (Mhow Indore district. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-080",
  "pinCode": "453441",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-081
**Objective**: Verify geo-fence validation for pinCode='453551'  
**Expected Decision**: ELIGIBLE (Sanwer Indore district. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-081",
  "pinCode": "453551",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-082
**Objective**: Verify geo-fence validation for pinCode='452111'  
**Expected Decision**: ELIGIBLE (Indore city peripheral. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-082",
  "pinCode": "452111",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-083
**Objective**: Verify geo-fence validation for pinCode='453115'  
**Expected Decision**: ELIGIBLE (Indore district rural. Inside service area.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-083",
  "pinCode": "453115",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-084
**Objective**: Verify geo-fence validation for pinCode='462001'  
**Expected Decision**: REJECTED (Bhopal PIN. Outside PRYME Indore service boundary.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-084",
  "pinCode": "462001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-085
**Objective**: Verify geo-fence validation for pinCode='400001'  
**Expected Decision**: REJECTED (Mumbai PIN. Outside PRYME Indore service boundary.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-085",
  "pinCode": "400001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-086
**Objective**: Verify geo-fence validation for pinCode='110001'  
**Expected Decision**: REJECTED (New Delhi PIN. Outside PRYME Indore service boundary.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-086",
  "pinCode": "110001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-087
**Objective**: Verify geo-fence validation for pinCode='560001'  
**Expected Decision**: REJECTED (Bengaluru PIN. Outside PRYME Indore service boundary.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-087",
  "pinCode": "560001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-088
**Objective**: Verify geo-fence validation for pinCode='380001'  
**Expected Decision**: REJECTED (Ahmedabad PIN. Outside PRYME Indore service boundary.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-088",
  "pinCode": "380001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-089
**Objective**: Verify geo-fence validation for pinCode='45200'  
**Expected Decision**: REJECTED (Malformed PIN (5 digits). Must fail validation.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-089",
  "pinCode": "45200",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-090
**Objective**: Verify geo-fence validation for pinCode='4520001'  
**Expected Decision**: REJECTED (Malformed PIN (7 digits). Must fail validation.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2000000.0,
  "propertyValue": 2500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-pincode-090",
  "pinCode": "4520001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-091
**Objective**: Verify edge cases for input=60000.01 with ICICI Bank  
**Expected Decision**: ELIGIBLE (60% FOIR (matches >= 60001.00 correctly due to .01 boundary check).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 60000.01,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-091",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-092
**Objective**: Verify edge cases for input=60000.00 with ICICI Bank  
**Expected Decision**: ELIGIBLE (50% FOIR (matches <= 60000.00 correctly. Decimals verified).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 60000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-092",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-093
**Objective**: Verify edge cases for input=30000.00 with ICICI Bank  
**Expected Decision**: ELIGIBLE (50% FOIR (exactly on the lower salary boundary).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 30000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-093",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-094
**Objective**: Verify edge cases for input=29999.99 with ICICI Bank  
**Expected Decision**: REJECTED (Below lowest salary floor of ₹30,000 (rejected on boundary check).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 29999.99,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-094",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-095
**Objective**: Verify edge cases for input=100000.01 with ICICI Bank  
**Expected Decision**: ELIGIBLE (65% FOIR (matches >= 100,001 slab accurately).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.01,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-095",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-096
**Objective**: Verify edge cases for input=100000.00 with ICICI Bank  
**Expected Decision**: ELIGIBLE (60% FOIR (matches <= 100,000 slab accurately).)  

```json
{
  "lenderId": 200,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-096",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-097
**Objective**: Verify edge cases for input=150000.00 with L&T Finance  
**Expected Decision**: ELIGIBLE (75% FOIR (matches <= 150000 bracket accurately).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 150000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-097",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-098
**Objective**: Verify edge cases for input=150000.01 with L&T Finance  
**Expected Decision**: ELIGIBLE (80% FOIR (matches >= 150001 'No Limit' bracket accurately).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 150000.01,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-098",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-099
**Objective**: Verify edge cases for input=250000.00 with L&T Finance  
**Expected Decision**: ELIGIBLE (80% FOIR (matches 'No Limit' upper bound slab).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 250000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-099",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-100
**Objective**: Verify edge cases for input=3000000.00 with L&T Finance  
**Expected Decision**: ELIGIBLE (90% LTV (requested loan amount exactly 30L is capped at 90% in L&T HL).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 3000000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-100",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-101
**Objective**: Verify edge cases for input=3000000.50 with L&T Finance  
**Expected Decision**: ELIGIBLE (80% LTV (requested loan amount slightly over 30L is capped at 80% in L&T HL).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 3000000.5,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-101",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-102
**Objective**: Verify edge cases for input=7500000.00 with L&T Finance  
**Expected Decision**: ELIGIBLE (80% LTV (requested loan amount exactly 75L is capped at 80% in L&T HL).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 7500000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-102",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-103
**Objective**: Verify edge cases for input=7500000.50 with L&T Finance  
**Expected Decision**: ELIGIBLE (75% LTV (requested loan amount slightly over 75L is capped at 75% in L&T HL).)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 7500000.5,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-103",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  }
}
```

---

### Scenario TC-104
**Objective**: Verify edge cases for input=Cash with L&T Finance  
**Expected Decision**: REJECTED (Salary mode check: 'Cash' is explicitly disallowed for L&T Home Loans.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-104",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  },
  "negativeSalaryMode": "Cash"
}
```

---

### Scenario TC-105
**Objective**: Verify edge cases for input=UPI with L&T Finance  
**Expected Decision**: REJECTED (Salary mode check: 'UPI' is explicitly disallowed for L&T Home Loans.)  

```json
{
  "lenderId": 73,
  "loanType": "HOME_LOAN",
  "cibilScore": 750,
  "applicantAge": 35,
  "employmentType": "SALARIED",
  "loanAmount": 2500000.0,
  "propertyValue": 3500000.0,
  "requestedTenureMonths": 240,
  "monthlyIncome": 100000.0,
  "existingEmiTotal": 0.0,
  "idempotencyKey": "test-spel-105",
  "pinCode": "452001",
  "incomeComputationInput": {
    "programName": "NIP"
  },
  "negativeSalaryMode": "UPI"
}
```

---

