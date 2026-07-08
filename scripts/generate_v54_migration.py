import json
import psycopg2
import psycopg2.extras
import re

DB_PARAMS = {
    'dbname': 'pryme_db',
    'user': 'postgres',
    'password': 'postgres',
    'host': 'localhost'
}

def get_db_data():
    conn = psycopg2.connect(**DB_PARAMS)
    cur = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)
    
    # Get all eligibility conditions
    cur.execute("SELECT id, product_code, bank_name, loan_type, employment_type, surrogate FROM eligibility_conditions")
    ec_rows = cur.fetchall()
    
    # Get all loan products to get product IDs and avoid duplicates
    cur.execute("SELECT id, product_code FROM loan_products")
    lp_rows = cur.fetchall()
    
    conn.close()
    return ec_rows, {r['product_code']: r['id'] for r in lp_rows}

def match_employment(db_emp, tsv_emp):
    if not db_emp:
        return False
    
    db_emp = db_emp.upper()
    tsv_emp = str(tsv_emp).upper()
    
    if "SALARIED" in tsv_emp and "SALARIED" in db_emp:
        return True
    
    is_tsv_sep = "SELF EMPLOYED" in tsv_emp or "SEP" in tsv_emp
    is_db_sep = "SEP" in db_emp or "SENP" in db_emp
    
    if is_tsv_sep and is_db_sep:
        return True
        
    return False

def match_surrogate(db_surrogate, tsv_surrogate):
    db_s = str(db_surrogate or 'NIP').upper().strip()
    tsv_s = str(tsv_surrogate or 'NIP').upper().strip()
    
    if tsv_s in ['NA', '']:
        tsv_s = 'NIP'
    if db_s in ['NA', '']:
        db_s = 'NIP'
        
    if tsv_s == 'LOW LTV': # some are marked LOW LTV in TSV but DB has NIP probably, wait we should map exactly or fallback
        # Let's map Low LTV to NIP for now, assuming it's a variant of NIP
        if db_s == 'NIP': return True
        
    # Standardize CPM SEP
    if 'CPM' in tsv_s and 'SEP' in tsv_s:
        tsv_s = 'CPM_SEP'
    
    return db_s == tsv_s

def parse_int_or_null(val):
    if not val or str(val).strip().upper() in ['NA', '']:
        return "NULL"
    nums = re.findall(r'\d+', str(val))
    if nums:
        return nums[0]
    return "NULL"

def parse_bool_or_null(val):
    if not val or str(val).strip().upper() in ['NA', '']:
        return "NULL"
    if str(val).strip().upper() == 'YES' or str(val).strip().upper() == 'TRUE':
        return "TRUE"
    return "FALSE"

def parse_text_or_null(val):
    if not val or str(val).strip().upper() in ['NA', '']:
        return "NULL"
    cleaned = str(val).replace("'", "''").strip()
    if cleaned == "":
        return "NULL"
    return f"'{cleaned}'"

LTV_JSON_GRID = """'{"Ready Built Property": {"tiers": [{"min": 0, "max": 3000000, "ltv": "90%"}, {"min": 3000001, "max": 7500000, "ltv": "80%"}, {"min": 7500001, "max": null, "ltv": "75%"}]}, "Plot": {"tiers": [{"min": 0, "max": null, "ltv": "70%"}]}}'::jsonb"""

def generate_migration():
    ec_rows, lp_map = get_db_data()
    
    lines = open("user_tsv_full.txt").readlines()
    
    # Find header line
    header_idx = -1
    for i, line in enumerate(lines):
        if '\tProduct_Name\t' in line or line.startswith('Product_Name\t'):
            header_idx = i
            break
            
    if header_idx == -1:
        for i, line in enumerate(lines):
            if '\tLoan_Type\tLender_Name\t' in line:
                header_idx = i
                break
                
    headers = lines[header_idx].strip().split('\t')
    # clean headers
    headers = [h.strip() for h in headers]
    if 'Product_Name' in headers[-1]: # handle the weird prefix
        pass # Not using strict header names, we'll use indexing based on known order
        
    # The actual known order:
    # 0: Product_Name, 1: Loan_Type, 2: Lender_Name, 3: Interest_Type, 4: Property_Type, 5: Negative_Property
    # 6: Employment_Type, 7: Self Employed Professional, 8: Surrogate, 9: Margin (by occupation), 10: LTV
    # 11: Formulae, 12: Conditions, 13: MIN_CIBIL, 14: Min_Tenure, 15: Max_Tenure, 16: Min_LoanAmount, 17: Max_LoanAmount
    # 18: Negative_Employer_Type, 19: Min_Income, 20: Min_Age, 21: Max_Age, 22: Negative_Profile, 23: Vintage
    # 24: ITR_Required, 25: Provident_Fund_Mandatory, 26: Negative_Mode_Salary, 27: Bank_Statement, 28: Salary_Slip_months
    # 29: GST_Required_months, 30: EMI_not_Obligated, 31: Admin Fee, 32: Insurance Charges, 33: Legal and Technical Charges
    # 34: Other_Expense, 35: Stamp Duty, 36: Prepayment Charges, 37: Foreclosure Charges, 38: Notes
    
    sql_statements = [
        "-- V54: Update eligibility conditions and loan products with TSV mappings",
        "ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS ltv_grid JSONB;",
        "ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS self_employed_professionals TEXT;",
        "ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS formulae TEXT;",
        ""
    ]
    
    processed_ec = set()
    processed_lp = set()
    
    for row_idx in range(header_idx + 1, len(lines)):
        row = lines[row_idx].strip().split('\t')
        if len(row) < 10 or row[0] == '': continue
        
        bank_name = row[2]
        loan_type = row[0] # TSV Product_Name is DB loan_type ('HL', 'LAP')
        emp_type = row[6]
        surrogate = row[8]
        
        # Match with EC
        matched_ecs = []
        for ec in ec_rows:
            # Fuzzy match bank name
            db_bank = ec['bank_name'].lower().replace(' bank', '').replace(' limited', '').replace(' ', '')
            tsv_bank = bank_name.lower().replace(' bank', '').replace(' limited', '').replace(' ', '')
            
            if db_bank == tsv_bank and ec['loan_type'] == loan_type:
                if match_employment(ec['employment_type'], emp_type):
                    if match_surrogate(ec['surrogate'], surrogate):
                        matched_ecs.append(ec)
                        
        if not matched_ecs:
            print(f"No match for: {bank_name} - {loan_type} - {emp_type} - {surrogate}")
            continue
            
        # Build UPDATE for eligibility_conditions
        # 4: Property_Type, 5: Negative_Property, 7: Self Employed Professional, 9: Margin, 10: LTV
        # 11: Formulae, 12: Conditions, 23: Vintage, 24: ITR_Required, 25: Provident_Fund_Mandatory,
        # 26: Negative_Mode_Salary, 27: Bank_Statement, 28: Salary_Slip_months, 29: GST_Required_months, 
        # 30: EMI_not_Obligated, 38: Notes (if length allows)
        
        prop_type = parse_text_or_null(row[4] if len(row) > 4 else '')
        neg_prop = parse_text_or_null(row[5] if len(row) > 5 else '')
        sep = parse_text_or_null(row[7] if len(row) > 7 else 'All Professions')
        margin = parse_text_or_null(row[9] if len(row) > 9 else '')
        
        ltv_raw = str(row[10] if len(row) > 10 else '').lower()
        if 'grid' in ltv_raw or 'sheet' in ltv_raw:
            ltv_grid = LTV_JSON_GRID
        else:
            # parse single value as json
            val = parse_text_or_null(row[10] if len(row) > 10 else '')
            if val != "NULL":
                ltv_grid = f"'{json.dumps({'default': val.strip(chr(39))})}'::jsonb"
            else:
                ltv_grid = "NULL"
                
        formulae = parse_text_or_null(row[11] if len(row) > 11 else '')
        cond = parse_text_or_null(row[12] if len(row) > 12 else '0')
        if cond == "NULL" or cond == "''": cond = "'0'"
        
        vintage = parse_text_or_null(row[23] if len(row) > 23 else '')
        itr = parse_int_or_null(row[24] if len(row) > 24 else '')
        pf = parse_bool_or_null(row[25] if len(row) > 25 else '')
        neg_mode = parse_text_or_null(row[26] if len(row) > 26 else '')
        bank_stmt = parse_text_or_null(row[27] if len(row) > 27 else '')
        sal_slip = parse_text_or_null(row[28] if len(row) > 28 else '')
        gst_req = parse_text_or_null(row[29] if len(row) > 29 else '')
        emi_not_ob = parse_text_or_null(row[30] if len(row) > 30 else '')
        notes = parse_text_or_null(row[38] if len(row) > 38 else '')
        
        for matched in matched_ecs:
            ec_id = matched['id']
            if ec_id not in processed_ec:
                sql = f"""UPDATE eligibility_conditions SET 
    property_type = {prop_type},
    negative_property = {neg_prop},
    self_employed_professionals = {sep},
    margin_by_occupation = {margin},
    ltv_grid = {ltv_grid},
    formulae = {formulae},
    conditions = {cond},
    vintage = {vintage},
    itr_required_years = {itr},
    provident_fund_mandatory = {pf},
    negative_salary_mode = {neg_mode},
    bank_statement_requirement = {bank_stmt},
    salary_slip_requirement = {sal_slip},
    gst_return_requirement = {gst_req},
    emi_not_obligated = {emi_not_ob},
    notes = {notes}
WHERE id = {ec_id};"""
                sql_statements.append(sql)
                processed_ec.add(ec_id)
            
            # Update loan_products for this product_code
            product_code = matched['product_code']
            if product_code in lp_map and product_code not in processed_lp:
                # 3: Interest_Type, 31: Admin Fee, 32: Insurance Charges, 33: Legal and Technical Charges,
                # 34: Other_Expense, 35: Stamp Duty, 36: Prepayment Charges, 37: Foreclosure Charges
                int_type = parse_text_or_null(row[3] if len(row) > 3 else '')
                admin = parse_text_or_null(row[31] if len(row) > 31 else '')
                insur = parse_text_or_null(row[32] if len(row) > 32 else '')
                legal = parse_text_or_null(row[33] if len(row) > 33 else '')
                other = parse_text_or_null(row[34] if len(row) > 34 else '')
                stamp = parse_text_or_null(row[35] if len(row) > 35 else '')
                prepay = parse_text_or_null(row[36] if len(row) > 36 else '')
                forec = parse_text_or_null(row[37] if len(row) > 37 else '')
                
                sql_lp = f"""UPDATE loan_products SET 
    interest_type = COALESCE({int_type}, interest_type),
    admin_fee = {admin},
    insurance_charges = {insur},
    legal_technical_charges = {legal},
    other_expense = {other},
    stamp_duties = {stamp},
    prepayment_charges = {prepay},
    foreclosure_charges = {forec}
WHERE product_code = '{product_code}';"""
                sql_statements.append(sql_lp)
                processed_lp.add(product_code)

    with open("src/main/resources/db/migration/V54__update_eligibility_and_loan_products.sql", "w") as f:
        f.write("\n".join(sql_statements))
        f.write("\n")
    print(f"Generated V54 with {len(sql_statements)} statements.")

if __name__ == '__main__':
    generate_migration()
