#!/usr/bin/env python3
"""
═══════════════════════════════════════════════════════════════════════════════
PRYME V36 Migration Generator — Excel → SQL Alignment
═══════════════════════════════════════════════════════════════════════════════
Reads the client's eligibility workbook Excel file (source of truth) and
generates a Flyway V36 migration that updates:

  1. loan_products table:
     min_cibil, min_loan_amount, max_loan_amount, min_tenure_months, max_tenure_months

  2. eligibility_conditions table:
     min_age, max_age, min_income (per product_code × employment_type × surrogate)

Login fees are NOT touched — V31 already seeds them correctly into
product_login_fee_matrix. The reconciliation script had a false-positive bug
where it compared against the stale loan_products.login_fees column that V31
deliberately nulled out.
═══════════════════════════════════════════════════════════════════════════════
"""
import os
import sys
from pathlib import Path
from collections import defaultdict
from datetime import datetime

try:
    import openpyxl
except ImportError:
    os.system(f"{sys.executable} -m pip install openpyxl -q")
    import openpyxl

# ─────────────────────────────────────────────────────────────────────────────
# PATHS
# ─────────────────────────────────────────────────────────────────────────────

DOWNLOADS = Path.home() / "Downloads"
PROJECT = Path.home() / "Documents" / "PRYME-BACKEND-PROD"
OUTPUT = PROJECT / "src" / "main" / "resources" / "db" / "migration" / "V36__align_db_with_client_excel.sql"
EXCEL_ELIG = DOWNLOADS / "eligibility workbook (1).xlsx"
EXCEL_LOGIN = DOWNLOADS / "Login_fees (1).xlsx"

# ─────────────────────────────────────────────────────────────────────────────
# LENDER → PRODUCT CODE PREFIX MAPPING
# ─────────────────────────────────────────────────────────────────────────────

LENDER_PREFIX = {
    "l&t finance":                  "LT",
    "l&t":                          "LT",
    "lt finance":                   "LT",
    "icici bank":                   "ICICI",
    "icici":                        "ICICI",
    "bandhan bank":                 "BANDHAN",
    "bandhan":                      "BANDHAN",
    "aditya birla finance limited": "ABFL",
    "aditya birla":                 "ABFL",
    "abfl":                         "ABFL",
    "bank of baroda":               "BOB",
    "bob":                          "BOB",
    "sbi":                          "SBI",
    "bajaj finance":                "BAJAJ",
    "bajaj prime":                  "BAJAJ",
    "bajaj":                        "BAJAJ",
    "yes bank":                     "YES",
    "yes":                          "YES",
    "hdfc bank":                    "HDFC",
    "hdfc":                         "HDFC",
    "jio finance":                  "JIO",
    "jio":                          "JIO",
    "idbi":                         "IDBI",
    "tata capital":                 "TATA",
    "tata":                         "TATA",
    "idfc":                         "IDFC",
    "idfc first bank":              "IDFC",
}

# V27 defaults (what's currently in the DB)
V27_DEFAULTS = {
    "min_cibil": 650,
    "max_cibil": 900,
    "min_loan_amount": 100000,
    "max_loan_amount": 999999999,
    "min_tenure_months": 12,
    "max_tenure_months": 360,
}
# V27 eligibility_conditions defaults
EC_DEFAULTS = {
    "min_age": 21,
    "max_age": 65,
    "min_income": 25000,
    "cibil_min": 650,
}


def safe_str(val):
    if val is None:
        return ""
    return str(val).strip()


def safe_int(val):
    """Convert to int, handling 'No Limit', floats, None."""
    if val is None:
        return None
    s = str(val).strip()
    if not s or s.lower() in ("na", "n/a", "-", ""):
        return None
    if s.lower().replace(" ", "") in ("nolimit", "nolimits", "unlimited"):
        return 999999999
    s = s.replace(",", "").replace("₹", "").replace(" ", "")
    try:
        return int(float(s))
    except (ValueError, TypeError):
        return None


def read_excel(filepath):
    wb = openpyxl.load_workbook(str(filepath), read_only=True, data_only=True)
    ws = wb.active
    rows = list(ws.iter_rows(values_only=True))
    wb.close()
    if not rows:
        return []
    headers = [safe_str(h) for h in rows[0]]
    data = []
    for row in rows[1:]:
        if all(v is None for v in row):
            continue
        rec = {}
        for i, h in enumerate(headers):
            if h and i < len(row):
                rec[h] = row[i]
        data.append(rec)
    return data


def resolve_product_code(lender_name, loan_type, emp_type):
    """Map (lender, loan_type, employment_type) → product code(s)."""
    prefix = LENDER_PREFIX.get(safe_str(lender_name).lower().strip())
    if not prefix:
        return []

    lt = safe_str(loan_type).upper().strip()
    if lt in ("HL", "HOME LOAN"):
        lt = "HL"
    elif lt in ("LAP", "LOAN AGAINST PROPERTY"):
        lt = "LAP"
    else:
        return []

    emp = safe_str(emp_type).strip()
    emp_lower = emp.lower()

    codes = []
    if "salaried" in emp_lower and "self" not in emp_lower:
        # Pure Salaried → -0001
        codes.append(f"{prefix}-{lt}-0001")
    elif "self employed" in emp_lower or "sep" in emp_lower or "senp" in emp_lower:
        # SEP/SENP → -0002
        codes.append(f"{prefix}-{lt}-0002")
        # Bajaj also has -0003 (Industry Margin variant) - same limits apply
        if prefix == "BAJAJ":
            codes.append(f"{prefix}-{lt}-0003")
    else:
        # Fallback: map to both
        codes.append(f"{prefix}-{lt}-0001")
        codes.append(f"{prefix}-{lt}-0002")

    return codes


def main():
    print("═" * 70)
    print("  PRYME V36 Migration Generator — Excel → SQL Alignment")
    print("═" * 70)

    # ── Read eligibility workbook
    print("\n[1/3] Reading eligibility workbook...")
    elig_rows = read_excel(EXCEL_ELIG)
    print(f"  {len(elig_rows)} rows")

    # ── Accumulate product-level and eligibility condition limits for aggregation
    product_limit_values = defaultdict(lambda: {
        "min_cibil": [],
        "min_tenure_months": [],
        "max_tenure_months": [],
        "min_loan_amount": [],
        "max_loan_amount": []
    })
    ec_limit_values = defaultdict(lambda: {
        "min_age": [],
        "max_age": [],
        "min_income": []
    })

    for row in elig_rows:
        lender = safe_str(row.get("Lender_Name", ""))
        loan_type = safe_str(row.get("Product_Name", ""))
        emp_type = safe_str(row.get("Employment_Type", ""))
        surrogate = safe_str(row.get("Surrogate", "NIP"))
        if not surrogate or surrogate.strip() == "":
            surrogate = "NIP"

        codes = resolve_product_code(lender, loan_type, emp_type)
        if not codes:
            continue

        # Accumulate product-level fields (from NIP/NA rows)
        if surrogate.upper() in ("NIP", "NA", ""):
            min_cibil = safe_int(row.get("MIN_CIBIL"))
            min_tenure = safe_int(row.get("Min_Tenure (Months)"))
            max_tenure = safe_int(row.get("Max_Tenure (Months)"))
            min_loan = safe_int(row.get("Min_LoanAmount"))
            max_loan = safe_int(row.get("Max_LoanAmount"))

            for code in codes:
                vals = product_limit_values[code]
                if min_cibil is not None:
                    vals["min_cibil"].append(min_cibil)
                if min_tenure is not None:
                    vals["min_tenure_months"].append(min_tenure)
                if max_tenure is not None:
                    vals["max_tenure_months"].append(max_tenure)
                if min_loan is not None:
                    vals["min_loan_amount"].append(min_loan)
                if max_loan is not None:
                    vals["max_loan_amount"].append(max_loan)

        # Accumulate eligibility conditions fields
        min_age = safe_int(row.get("Min_Age"))
        max_age = safe_int(row.get("Max_Age"))
        min_income = safe_int(row.get("Min_Income"))

        for code in codes:
            emp_lower = emp_type.lower()
            if "salaried" in emp_lower and "self" not in emp_lower:
                canonical_emp = "Salaried"
            else:
                canonical_emp = "Self Employed"
            
            key = (code, surrogate.upper(), canonical_emp)
            vals = ec_limit_values[key]
            if min_age is not None:
                vals["min_age"].append(min_age)
            if max_age is not None:
                vals["max_age"].append(max_age)
            if min_income is not None:
                vals["min_income"].append(min_income)

    # Compute final aggregated values
    product_updates = {}
    for code, vals in product_limit_values.items():
        pu = {}
        if vals["min_cibil"]:
            pu["min_cibil"] = min(vals["min_cibil"])
        if vals["min_tenure_months"]:
            pu["min_tenure_months"] = min(vals["min_tenure_months"])
        if vals["max_tenure_months"]:
            pu["max_tenure_months"] = max(vals["max_tenure_months"])
        if vals["min_loan_amount"]:
            pu["min_loan_amount"] = min(vals["min_loan_amount"])
        if vals["max_loan_amount"]:
            pu["max_loan_amount"] = max(vals["max_loan_amount"])
        if pu:
            product_updates[code] = pu

    # Filter out unchanged product updates
    real_updates = {}
    for code, updates in product_updates.items():
        changes = {}
        for field, excel_val in updates.items():
            db_default = V27_DEFAULTS.get(field)
            if db_default is not None and excel_val != db_default:
                changes[field] = excel_val
        if changes:
            real_updates[code] = changes

    # Generate eligibility conditions updates list
    ec_updates = []
    for (code, surrogate, emp_type), vals in ec_limit_values.items():
        ec_updates.append({
            "product_code": code,
            "surrogate": surrogate,
            "emp_type": emp_type,
            "min_age": min(vals["min_age"]) if vals["min_age"] else None,
            "max_age": max(vals["max_age"]) if vals["max_age"] else None,
            "min_income": min(vals["min_income"]) if vals["min_income"] else None,
        })

    print(f"\n[2/3] Computed changes:")
    print(f"  Product-level updates: {len(real_updates)} product codes")
    print(f"  Eligibility condition updates: {len(ec_updates)} rows")

    # ── Generate SQL
    print(f"\n[3/3] Generating V36 migration...")
    lines = []
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    lines.append("-- ═══════════════════════════════════════════════════════════════════════════════")
    lines.append("-- V36 — ALIGN DATABASE WITH CLIENT EXCEL SHEETS (Source of Truth)")
    lines.append("-- ═══════════════════════════════════════════════════════════════════════════════")
    lines.append(f"-- Generated: {now}")
    lines.append("-- Source: eligibility workbook (1).xlsx, Login_fees (1).xlsx")
    lines.append("-- Strategy: UPDATE existing rows to match client-specified policy limits.")
    lines.append("--")
    lines.append("-- WHAT THIS FIXES:")
    lines.append("--   1. loan_products: min_cibil, min/max_loan_amount, min/max_tenure_months")
    lines.append("--      were seeded with uniform defaults (650, 100K, 999M, 12, 360) in V27.")
    lines.append("--      Client Excel specifies lender-specific values.")
    lines.append("--   2. eligibility_conditions: min_age, max_age, min_income were seeded with")
    lines.append("--      defaults (21, 65, 25000) in V27. Client Excel specifies per-product values.")
    lines.append("--")
    lines.append("-- WHAT THIS DOES NOT TOUCH:")
    lines.append("--   - product_login_fee_matrix (V31 already has correct dynamic login fees)")
    lines.append("--   - product_pf_matrix (V29 already has correct PF data)")
    lines.append("--   - product_roi_matrix (V27 already has correct ROI tiers)")
    lines.append("--   - Enrichment columns from V32 (negative lists, formulae, etc.)")
    lines.append("-- ═══════════════════════════════════════════════════════════════════════════════")
    lines.append("")

    # ── PART 1: loan_products updates
    lines.append("-- ─────────────────────────────────────────────────────────────────────────────")
    lines.append("-- PART 1: LOAN PRODUCT LIMITS — per product_code")
    lines.append("-- ─────────────────────────────────────────────────────────────────────────────")
    lines.append("")

    # Group by lender prefix for readability
    by_lender = defaultdict(list)
    for code in sorted(real_updates.keys()):
        prefix = code.rsplit("-", 1)[0].rsplit("-", 1)[0]  # e.g., "ABFL" from "ABFL-HL-0001"
        by_lender[prefix].append(code)

    for lender_prefix in sorted(by_lender.keys()):
        codes = by_lender[lender_prefix]
        lines.append(f"-- ═══ {lender_prefix} ═══")
        for code in codes:
            changes = real_updates[code]
            set_clauses = []
            for field, val in sorted(changes.items()):
                set_clauses.append(f"{field} = {val}")
            if set_clauses:
                lines.append(f"UPDATE loan_products SET {', '.join(set_clauses)} WHERE product_code = '{code}';")
        lines.append("")

    # ── PART 2: eligibility_conditions updates
    lines.append("-- ─────────────────────────────────────────────────────────────────────────────")
    lines.append("-- PART 2: ELIGIBILITY CONDITIONS — min_age, max_age, min_income")
    lines.append("-- ─────────────────────────────────────────────────────────────────────────────")
    lines.append("-- V27 seeded defaults: min_age=21, max_age=65, min_income=25000 for all.")
    lines.append("-- Client Excel specifies lender-specific values per employment type × surrogate.")
    lines.append("")

    # Deduplicate and group by product_code
    seen_ec = set()
    ec_by_code = defaultdict(list)
    for ec in ec_updates:
        key = (ec["product_code"], ec["surrogate"], ec.get("emp_type", ""))
        if key in seen_ec:
            continue
        seen_ec.add(key)
        ec_by_code[ec["product_code"]].append(ec)

    # Build employment type WHERE clause mapping
    def emp_type_where(emp_str):
        """Return SQL WHERE clause fragment for employment_type matching."""
        emp_lower = emp_str.lower().strip()
        if "salaried" in emp_lower and "self" not in emp_lower:
            return "employment_type IN ('Salaried', 'SALARIED_SEP')"
        elif "self employed" in emp_lower:
            return "employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP')"
        else:
            return None

    def surrogate_where(surr):
        s = surr.strip().upper()
        if s == "NIP" or s == "":
            return "(surrogate = 'NIP' OR surrogate IS NULL)"
        else:
            return f"surrogate = '{s}'"

    for code in sorted(ec_by_code.keys()):
        prefix = code.split("-")[0]
        lines.append(f"-- {code}")
        for ec in ec_by_code[code]:
            set_parts = []
            has_change = False

            if ec["min_age"] is not None and ec["min_age"] != EC_DEFAULTS["min_age"]:
                set_parts.append(f"min_age = {ec['min_age']}")
                has_change = True
            if ec["max_age"] is not None and ec["max_age"] != EC_DEFAULTS["max_age"]:
                set_parts.append(f"max_age = {ec['max_age']}")
                has_change = True
            if ec["min_income"] is not None and ec["min_income"] != EC_DEFAULTS["min_income"]:
                set_parts.append(f"min_income = {ec['min_income']}")
                has_change = True

            if not has_change:
                continue

            emp_where = emp_type_where(ec.get("emp_type", ""))
            surr_where = surrogate_where(ec.get("surrogate", "NIP"))

            where_parts = [f"product_code = '{code}'"]
            if emp_where:
                where_parts.append(emp_where)
            where_parts.append(surr_where)

            lines.append(f"UPDATE eligibility_conditions SET {', '.join(set_parts)} WHERE {' AND '.join(where_parts)};")
        lines.append("")

    # ── Write output
    sql = "\n".join(lines)
    OUTPUT.write_text(sql)

    total_product_updates = sum(len(v) for v in real_updates.values())
    print(f"\n{'═' * 70}")
    print(f"  ✅ Migration written to: {OUTPUT}")
    print(f"  Product codes updated: {len(real_updates)}")
    print(f"  Total field-level changes: {total_product_updates}")
    print(f"{'═' * 70}")


if __name__ == "__main__":
    main()
