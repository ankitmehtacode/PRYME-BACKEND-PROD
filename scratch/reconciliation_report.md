# PRYME Engine — Excel ↔ Database Reconciliation Report

> Generated: 2026-06-17 14:59:29
> Excel Sheets: 6 files from ~/Downloads
> DB Source: SQL migrations V27–V35

---

## 1. Executive Summary

| Metric | Count |
|--------|-------|
| Excel Lender×LoanType pairs | 25 |
| DB Lender×LoanType pairs | 26 |
| **Missing from DB** | **0** |
| **Extra in DB** | **1** |
| Common (matched) | 25 |
| **Total Discrepancies** | **0** |
| Eligibility Workbook rows | 155 |
| FOIR Sheet rows | 130 |
| PF Sheet rows | 57 |
| Login Fee Sheet rows | 50 |
| HL LTV Sheet rows | 7 |
| LAP LTV Sheet rows | 42 |
| DB Product Codes | 54 |

---

## 2. Products in Excel but MISSING from Database

✅ No products missing from DB — all Excel products have DB entries.


---

## 3. Products in Database but NOT in Excel (Extra/Stale)

> [!WARNING]
> These lender×loan-type combinations exist in the DB but are NOT in the client's Excel sheets. They may be stale or test data.

| # | Lender | Loan Type | Status |
|---|--------|-----------|--------|
| 1 | ICICI HFC | LAP | ⚠️ EXTRA |

---

## 4. Eligibility Attribute Discrepancies (Excel vs DB)

✅ All eligibility attributes match between Excel and DB.


---

## 5. FOIR Slab Discrepancies

✅ All FOIR slabs have corresponding DB entries.


---

## 6. Login Fee Discrepancies

✅ All login fees match.


---

## 7. Processing Fee (PF) Discrepancies

✅ All PF entries have corresponding DB entries.


---

## 8. Full Product Catalog Cross-Reference

| Lender | Loan Type | In Excel | In DB | Status |
|--------|-----------|----------|-------|--------|
| ABFL | HL | ✅ | ✅ | ✅ MATCHED |
| ABFL | LAP | ✅ | ✅ | ✅ MATCHED |
| Bajaj Finance | HL | ✅ | ✅ | ✅ MATCHED |
| Bajaj Finance | LAP | ✅ | ✅ | ✅ MATCHED |
| Bandhan Bank | HL | ✅ | ✅ | ✅ MATCHED |
| Bandhan Bank | LAP | ✅ | ✅ | ✅ MATCHED |
| Bank of Baroda | HL | ✅ | ✅ | ✅ MATCHED |
| Bank of Baroda | LAP | ✅ | ✅ | ✅ MATCHED |
| HDFC Bank | HL | ✅ | ✅ | ✅ MATCHED |
| HDFC Bank | LAP | ✅ | ✅ | ✅ MATCHED |
| ICICI Bank | HL | ✅ | ✅ | ✅ MATCHED |
| ICICI Bank | LAP | ✅ | ✅ | ✅ MATCHED |
| ICICI HFC | LAP | ❌ | ✅ | ⚠️ EXTRA IN DB |
| IDBI | HL | ✅ | ✅ | ✅ MATCHED |
| IDBI | LAP | ✅ | ✅ | ✅ MATCHED |
| IDFC | LAP | ✅ | ✅ | ✅ MATCHED |
| JIO Finance | HL | ✅ | ✅ | ✅ MATCHED |
| JIO Finance | LAP | ✅ | ✅ | ✅ MATCHED |
| L&T Finance | HL | ✅ | ✅ | ✅ MATCHED |
| L&T Finance | LAP | ✅ | ✅ | ✅ MATCHED |
| SBI | HL | ✅ | ✅ | ✅ MATCHED |
| SBI | LAP | ✅ | ✅ | ✅ MATCHED |
| Tata Capital | HL | ✅ | ✅ | ✅ MATCHED |
| Tata Capital | LAP | ✅ | ✅ | ✅ MATCHED |
| YES BANK | HL | ✅ | ✅ | ✅ MATCHED |
| YES BANK | LAP | ✅ | ✅ | ✅ MATCHED |

---

## 9. Excel Sheet Audit Summary

| Sheet | File | Rows Read | Lenders Found |
|-------|------|-----------|---------------|
| Eligibility Workbook | eligibility workbook (1).xlsx | 155 | 13 |
| FOIR Sheet | FOIR_Sheet (1).xlsx | 130 | 13 |
| PF Data | PF_data (1).xlsx | 57 | 13 |
| Login Fees | Login_fees (1).xlsx | 50 | 13 |
| HL LTV | HL_LTV_Sheet.xlsx | 7 | — |
| LAP LTV | LAP_LTV_Sheet.xlsx | 42 | — |
