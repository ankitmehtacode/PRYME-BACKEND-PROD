#!/usr/bin/env python3
"""
Parse the FOIR workbook data and generate V58 migration SQL.
Maps each (lender, loan_type) to all DB products missing FOIR rows.
"""

# Lender name mapping: workbook → DB
LENDER_MAP = {
    "L&T Finance": "L&T Finance",
    "ICICI Bank": "ICICI Bank",
    "Bandhan Bank": "Bandhan Bank",
    "Aditya Birla Finance Limited": "Aditya Birla Finance Limited",
    "Bank of Baroda": "Bank of Baroda",
    "SBI": "SBI",
    "Bajaj Prime": "Bajaj Finance",
    "Yes Bank": "YES BANK",
    "HDFC": "HDFC Bank",
    "JIO Finance": "JIO Finance",
    "IDBI": "IDBI",
    "TATA Capital": "Tata Capital",
    "Indus Ind Bank": "Indus Ind Bank",
    "IDFC": "IDFC",
}

def parse_salary(s):
    """Convert salary string to SQL value."""
    if s is None or s.strip() == '' or s.strip() == ' ':
        return "NULL"
    s = s.strip()
    if s.lower() in ('no limit', 'no limit'):
        return "NULL"
    return s.replace(',', '')

def parse_foir(s):
    """Convert FOIR% string to decimal. Handle special cases."""
    s = s.strip().replace('%', '')
    # Formula-based
    if '140-LTV' in s or '140-ltv' in s.lower():
        return "0.6500"  # Assuming ~75% LTV → 140-75=65%
    if '170-LTV' in s or '170-ltv' in s.lower():
        return "0.9500"  # Assuming ~75% LTV → 170-75=95%
    if '(' in s:
        s = s.replace('(', '').replace(')', '')
    val = float(s) / 100.0
    return f"{val:.4f}"

def parse_deviation(s):
    """Convert deviation% string to decimal."""
    if s is None or s.strip() == '':
        return "NULL"
    s = s.strip().replace('%', '')
    if '-' in s:
        # Range like "5-15", use the lower bound (conservative)
        parts = s.split('-')
        val = float(parts[0]) / 100.0
    else:
        val = float(s) / 100.0
    return f"{val:.4f}"

# Raw workbook data: (product_name, lender_workbook, surrogate, emp_type, lower, upper, foir_pct, deviation)
HL_DATA = [
    # L&T Finance
    ("L&T Finance", "NIP", "Salaried", "30000", "50000", "60%", ""),
    ("L&T Finance", "NIP", "Salaried", "50001", "75000", "70%", ""),
    ("L&T Finance", "NIP", "Salaried", "75001", "150000", "75%", ""),
    ("L&T Finance", "NIP", "Salaried", "150001", "No Limit", "80%", ""),
    ("L&T Finance", "NIP", "Self Employed Professional/Self Employed Non Professional", "25000", "No Limit", "85%", ""),
    ("L&T Finance", "SEP", "Self Employed Professional", "", "", "75%", ""),
    ("L&T Finance", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "55%", ""),
    ("L&T Finance", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "65%", ""),
    # ICICI Bank
    ("ICICI Bank", "NIP", "Salaried", "30000", "60000", "50%", "5%"),
    ("ICICI Bank", "NIP", "Salaried", "60001", "100000", "60%", "5%"),
    ("ICICI Bank", "NIP", "Salaried", "100001", "200000", "65%", "5%"),
    ("ICICI Bank", "NIP", "Salaried", "200001", "No Limit", "70%", "5%"),
    ("ICICI Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "200000", "No Limit", "140-LTV%", "5%"),
    ("ICICI Bank", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "33%", "5%"),
    ("ICICI Bank", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "99%", "5%"),
    # Bandhan Bank
    ("Bandhan Bank", "NIP", "Salaried", "15000", "No Limit", "65%", ""),
    ("Bandhan Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "15000", "No Limit", "65%", ""),
    ("Bandhan Bank", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "60%", ""),
    ("Bandhan Bank", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", ""),
    # Aditya Birla Finance Limited
    ("Aditya Birla Finance Limited", "NIP", "Salaried", "0", "300000", "60%", ""),
    ("Aditya Birla Finance Limited", "NIP", "Salaried", "300001", "600000", "65%", ""),
    ("Aditya Birla Finance Limited", "NIP", "Salaried", "600001", "No limit", "70%", ""),
    ("Aditya Birla Finance Limited", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "150%", ""),
    ("Aditya Birla Finance Limited", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "60%", ""),
    ("Aditya Birla Finance Limited", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "150%", ""),
    # Bank of Baroda
    ("Bank of Baroda", "NIP", "Salaried", "10000", "75000", "50%", ""),
    ("Bank of Baroda", "NIP", "Salaried", "75001", "300000", "60%", ""),
    ("Bank of Baroda", "NIP", "Salaried", "300001", "No Limit", "70%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional", "300001", "500000", "75%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional", "500001", "No Limit", "80%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional/Self Employed Non Professional", "10000", "75000", "50%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional/Self Employed Non Professional", "75001", "300000", "60%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Non Professional", "300001", "No Limit", "70%", ""),
    # SBI
    ("SBI", "NIP", "Salaried", "25000", "41666", "50%", "5-15%"),
    ("SBI", "NIP", "Salaried", "41667", "66666", "60%", "5-15%"),
    ("SBI", "NIP", "Salaried", "66667", "83333", "65%", "5-15%"),
    ("SBI", "NIP", "Salaried", "83334", "No Limit", "70%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "25000", "41666", "50%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "41667", "66666", "60%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "66667", "83333", "65%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "83334", "No Limit", "70%", "5-15%"),
    # Bajaj Prime → Bajaj Finance
    ("Bajaj Prime", "NIP", "Salaried", "", "", "70%", ""),
    ("Bajaj Prime", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", "20%"),
    ("Bajaj Prime", "SEP", "Self Employed Professional", "", "", "100%", ""),
    ("Bajaj Prime", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "66%", ""),
    ("Bajaj Prime", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "80%", ""),
    # Yes Bank → YES BANK
    ("Yes Bank", "NIP", "Salaried", "40000", "100000", "70%", ""),
    ("Yes Bank", "NIP", "Salaried", "100001", "No Limit", "75%", ""),
    ("Yes Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", "20%"),
    ("Yes Bank", "SEP", "Self Employed Professional", "", "", "80%", ""),
    ("Yes Bank", "CPM SEP", "Self Employed Professional", "", "", "75%", ""),
    ("Yes Bank", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "66%", ""),
    ("Yes Bank", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "70%", ""),
    # HDFC → HDFC Bank
    ("HDFC", "NIP", "Salaried", "", "", "80%", ""),
    ("HDFC", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "80%", ""),
    ("HDFC", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "65%", ""),
    # JIO Finance
    ("JIO Finance", "NIP", "Salaried", "30000", "50000", "55%", ""),
    ("JIO Finance", "NIP", "Salaried", "50001", "100000", "65%", ""),
    ("JIO Finance", "NIP", "Salaried", "100001", "No Limit", "70%", ""),
    ("JIO Finance", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "80%", ""),
    ("JIO Finance", "SEP", "Self Employed Professional", "", "", "70%", ""),
    ("JIO Finance", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "75%", ""),
    ("JIO Finance", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "75%", ""),
    # IDBI
    ("IDBI", "NIP", "Salaried", "", "", "75%", ""),
    ("IDBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "70%", ""),
    # TATA Capital
    ("TATA Capital", "NIP", "Salaried", "", "", "65%", ""),
    ("TATA Capital", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", ""),
    ("TATA Capital", "SEP", "Self Employed Professional", "", "", "100%", ""),
    # Indus Ind Bank
    ("Indus Ind Bank", "NIP", "Salaried", "20000", "50000", "60%", ""),
    ("Indus Ind Bank", "NIP", "Salaried", "50001", "100001", "70%", ""),
    ("Indus Ind Bank", "NIP", "Salaried", "100001", "No Limit", "75%", ""),
    ("Indus Ind Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "20000", "No Limit", "(170-LTV)%", ""),
    ("Indus Ind Bank", "SEP", "Self Employed Professional", "", "", "70%", ""),
]

LAP_DATA = [
    # L&T Finance
    ("L&T Finance", "NIP", "Salaried", "30000", "50000", "55%", ""),
    ("L&T Finance", "NIP", "Salaried", "50001", "75000", "65%", ""),
    ("L&T Finance", "NIP", "Salaried", "75001", "150000", "70%", ""),
    ("L&T Finance", "NIP", "Salaried", "150001", "No Limit", "75%", ""),
    ("L&T Finance", "NIP", "Self Employed Professional/Self Employed Non Professional", "25000", "No Limit", "75%", ""),
    ("L&T Finance", "SEP", "Self Employed Professional", "", "", "75%", ""),
    ("L&T Finance", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "55%", "10%"),
    ("L&T Finance", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "65%", ""),
    # ICICI Bank
    ("ICICI Bank", "NIP", "Salaried", "30000", "60000", "50%", "5%"),
    ("ICICI Bank", "NIP", "Salaried", "60001", "100000", "60%", "5%"),
    ("ICICI Bank", "NIP", "Salaried", "100001", "200000", "65%", "5%"),
    ("ICICI Bank", "NIP", "Salaried", "200001", "No Limit", "70%", "5%"),
    ("ICICI Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "200000", "No Limit", "140-LTV%", "5%"),
    ("ICICI Bank", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "33%", "5%"),
    ("ICICI Bank", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "99%", ""),
    # Bandhan Bank
    ("Bandhan Bank", "NIP", "Salaried", "15000", "No Limit", "65%", ""),
    ("Bandhan Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "15000", "No Limit", "65%", ""),
    ("Bandhan Bank", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "60%", ""),
    ("Bandhan Bank", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", ""),
    # Aditya Birla Finance Limited
    ("Aditya Birla Finance Limited", "NIP", "Salaried", "0", "300000", "60%", ""),
    ("Aditya Birla Finance Limited", "NIP", "Salaried", "300001", "600000", "65%", ""),
    ("Aditya Birla Finance Limited", "NIP", "Salaried", "600001", "No limit", "70%", ""),
    ("Aditya Birla Finance Limited", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "150%", ""),
    ("Aditya Birla Finance Limited", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "60%", ""),
    ("Aditya Birla Finance Limited", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "150%", ""),
    # Bank of Baroda
    ("Bank of Baroda", "NIP", "Salaried", "10000", "75000", "50%", ""),
    ("Bank of Baroda", "NIP", "Salaried", "75001", "300000", "60%", ""),
    ("Bank of Baroda", "NIP", "Salaried", "300001", "No Limit", "70%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional", "300001", "500000", "75%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional", "500001", "No Limit", "80%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional/Self Employed Non Professional", "10000", "75000", "50%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Professional/Self Employed Non Professional", "75001", "300000", "60%", ""),
    ("Bank of Baroda", "NIP", "Self Employed Non Professional", "300001", "No Limit", "70%", ""),
    # SBI
    ("SBI", "NIP", "Salaried", "25000", "41666", "50%", "5-15%"),
    ("SBI", "NIP", "Salaried", "41667", "83333", "55%", "5-15%"),
    ("SBI", "NIP", "Salaried", "83334", "No Limit", "60%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "25000", "41666", "50%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "41667", "83333", "55%", "5-15%"),
    ("SBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "83334", "No Limit", "60%", "5-15%"),
    # Bajaj Prime → Bajaj Finance
    ("Bajaj Prime", "NIP", "Salaried", "", "", "100%", ""),
    ("Bajaj Prime", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", "20%"),
    ("Bajaj Prime", "SEP", "Self Employed Professional", "", "", "100%", ""),
    ("Bajaj Prime", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "66%", ""),
    ("Bajaj Prime", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "80%", ""),
    # Yes Bank → YES BANK
    ("Yes Bank", "NIP", "Salaried", "40000", "100000", "70%", ""),
    ("Yes Bank", "NIP", "Salaried", "100001", "No Limit", "75%", ""),
    ("Yes Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", "20%"),
    ("Yes Bank", "SEP", "Self Employed Professional", "", "", "80%", ""),
    ("Yes Bank", "CPM SEP", "Self Employed Professional", "", "", "75%", ""),
    ("Yes Bank", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "66%", ""),
    ("Yes Bank", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "70%", ""),
    # HDFC → HDFC Bank
    ("HDFC", "NIP", "Salaried", "", "", "70%", ""),
    ("HDFC", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "70%", "20%"),
    ("HDFC", "Banking", "Self Employed Professional/Self Employed Non Professional", "", "", "80%", ""),
    ("HDFC", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "65%", ""),
    # IDFC
    ("IDFC", "NIP", "Salaried", "", "", "75%", ""),
    ("IDFC", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "150%", "20%"),
    ("IDFC", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "75%", ""),
    # JIO Finance
    ("JIO Finance", "NIP", "Salaried", "", "", "75%", ""),
    ("JIO Finance", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "80%", ""),
    ("JIO Finance", "SEP", "Self Employed Professional", "", "", "70%", ""),
    ("JIO Finance", "GST", "Self Employed Professional/Self Employed Non Professional", "", "", "75%", ""),
    # IDBI
    ("IDBI", "NIP", "Salaried", "", "", "75%", ""),
    ("IDBI", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "70%", ""),
    # TATA Capital
    ("TATA Capital", "NIP", "Salaried", "", "", "65%", ""),
    ("TATA Capital", "NIP", "Self Employed Professional/Self Employed Non Professional", "", "", "100%", ""),
    ("TATA Capital", "SEP", "Self Employed Professional", "", "", "100%", ""),
    # Indus Ind Bank
    ("Indus Ind Bank", "NIP", "Salaried", "20000", "50000", "60%", ""),
    ("Indus Ind Bank", "NIP", "Salaried", "50001", "100001", "70%", ""),
    ("Indus Ind Bank", "NIP", "Salaried", "100001", "No Limit", "75%", ""),
    ("Indus Ind Bank", "NIP", "Self Employed Professional/Self Employed Non Professional", "20000", "No Limit", "(170-LTV)%", ""),
    ("Indus Ind Bank", "SEP", "Self Employed Professional", "", "", "70%", ""),
]


def generate_values_block(data_list):
    """Generate the VALUES (...), (...) block from data."""
    lines = []
    for i, (lender_wb, surrogate, emp_type, lower, upper, foir_pct, deviation) in enumerate(data_list):
        mn = parse_salary(lower)
        mx = parse_salary(upper)
        f = parse_foir(foir_pct)
        d = parse_deviation(deviation)

        # Format salary values
        mn_sql = f"{mn}::numeric" if mn != "NULL" else "NULL::numeric"
        mx_sql = f"{mx}::numeric" if mx != "NULL" else "NULL::numeric"
        d_sql = f"{d}::numeric" if d != "NULL" else "NULL::numeric"

        sep = "," if i < len(data_list) - 1 else ""
        lines.append(f"    ('{emp_type}', '{surrogate}', {mn_sql}, {mx_sql}, {f}::numeric, {d_sql}){sep}")

    return "\n".join(lines)


def generate_insert_block(db_lender, loan_type, data_rows):
    """Generate one INSERT...SELECT block."""
    values = generate_values_block(data_rows)
    return f"""INSERT INTO product_foir_matrix (product_id, employment_type, surrogate, min_salary, max_salary, foir, deviation)
SELECT lp.id, v.et, v.surr, v.mn, v.mx, v.f, v.d
FROM loan_products lp
CROSS JOIN (VALUES
{values}
) AS v(et, surr, mn, mx, f, d)
WHERE lp.lender_name = '{db_lender}'
  AND lp.loan_type = '{loan_type}'
  AND lp.is_active = true
  AND NOT EXISTS (SELECT 1 FROM product_foir_matrix pfm WHERE pfm.product_id = lp.id);"""


def main():
    # Group data by (db_lender_name)
    # For HL data → insert into loan_type='HL' products
    # For LAP data → insert into loan_type='LAP' products (covers both LAP-0002 and Secured-*)

    lines = []
    lines.append("-- V58__seed_missing_foir_matrix.sql")
    lines.append("-- ==============================================================================")
    lines.append("-- Seeds the product_foir_matrix table for all active products that currently")
    lines.append("-- have ZERO FOIR rows. Data sourced from PRYME FOIR workbook (2026-07-08).")
    lines.append("--")
    lines.append("-- For each (lender, loan_type), inserts ALL FOIR rows from the workbook into")
    lines.append("-- every product of that lender/loan_type that has no FOIR data yet.")
    lines.append("-- The engine's runtime matchEmploymentType + surrogate filter ensures only")
    lines.append("-- the correct FOIR row is used for each applicant profile.")
    lines.append("--")
    lines.append("-- Special FOIR values:")
    lines.append("--   140-LTV% (ICICI SEP/SENP) → stored as 0.6500 (assuming ~75% LTV)")
    lines.append("--   (170-LTV)% (IndusInd SEP/SENP) → stored as 0.9500 (assuming ~75% LTV)")
    lines.append("--   5-15% deviation (SBI) → stored as 0.0500 (conservative lower bound)")
    lines.append("-- ==============================================================================")
    lines.append("")

    # Group HL data by lender
    hl_by_lender = {}
    for row in HL_DATA:
        lender_wb = row[0]
        db_lender = LENDER_MAP[lender_wb]
        hl_by_lender.setdefault(db_lender, []).append(row)

    # Group LAP data by lender
    lap_by_lender = {}
    for row in LAP_DATA:
        lender_wb = row[0]
        db_lender = LENDER_MAP[lender_wb]
        lap_by_lender.setdefault(db_lender, []).append(row)

    # Generate HL inserts
    lines.append("-- ══════════════════════════════════════════════════════════════════════════════")
    lines.append("-- HL PRODUCTS (loan_type = 'HL')")
    lines.append("-- Covers: *-HL-0002, *-HL-0003 products")
    lines.append("-- ══════════════════════════════════════════════════════════════════════════════")
    lines.append("")

    for db_lender in sorted(hl_by_lender.keys()):
        rows = hl_by_lender[db_lender]
        lines.append(f"-- ── {db_lender} (HL) ──")
        lines.append(generate_insert_block(db_lender, "HL", rows))
        lines.append("")

    # Generate LAP inserts
    lines.append("-- ══════════════════════════════════════════════════════════════════════════════")
    lines.append("-- LAP PRODUCTS (loan_type = 'LAP')")
    lines.append("-- Covers: *-LAP-0002, *-LAP-0003, *-Secured-* products")
    lines.append("-- ══════════════════════════════════════════════════════════════════════════════")
    lines.append("")

    for db_lender in sorted(lap_by_lender.keys()):
        rows = lap_by_lender[db_lender]
        lines.append(f"-- ── {db_lender} (LAP) ──")
        lines.append(generate_insert_block(db_lender, "LAP", rows))
        lines.append("")

    # Also handle ICICI HFC (no workbook data → use ICICI Bank LAP data as proxy)
    lines.append("-- ── ICICI HFC (LAP) — Using ICICI Bank LAP FOIR as proxy ──")
    icici_lap = lap_by_lender.get("ICICI Bank", [])
    if icici_lap:
        lines.append(generate_insert_block("ICICI HFC", "LAP", icici_lap))
        lines.append("")

    print("\n".join(lines))


if __name__ == "__main__":
    main()
