import csv
import re

def parse_ltv(ltv_str):
    if not ltv_str or "As per" in ltv_str or "NA" in ltv_str:
        return "NULL"
    m = re.search(r'(\d+)%', ltv_str)
    if m:
        val = int(m.group(1))
        return f"{val / 100.0:.2f}"
    return "NULL"

def parse_num(s):
    if not s or s.strip() == "NA" or s.strip() == "No Limit":
        return "NULL"
    try:
        return str(int(float(s.strip().replace(',',''))))
    except:
        return "NULL"

sql_statements = []

with open('/Users/manishmehta/Documents/PRYME-BACKEND-PROD/scratch/source_of_truth_v2.md', 'r') as f:
    reader = csv.DictReader(f, delimiter='\t')
    
    for row in reader:
        product_name = row.get('Product_Name', '').strip()
        if not product_name: continue
        db_product = 'HOME LOAN' if product_name == 'HL' else 'LOAN AGAINST PROPERTY'
        
        lender = row.get('Lender_Name', '').strip()
        if not lender: continue
        
        emp_type_raw = row.get('Employment_Type', '').strip()
        db_emp_types = []
        if 'Salaried' in emp_type_raw:
            db_emp_types.append('Salaried')
        if 'Self Employed Professional' in emp_type_raw:
            db_emp_types.append('Self Employed Professional')
        if 'Self Employed Non Professional' in emp_type_raw:
            db_emp_types.append('Self Employed Non Professional')
        if not db_emp_types: continue
        
        surrogate_raw = row.get('Surrogate', '').strip().upper()
        if not surrogate_raw: continue
        
        if surrogate_raw == 'NIP':
            db_surrogate = 'NIP'
        elif surrogate_raw == 'LOW LTV':
            db_surrogate = 'LOW_LTV'
        elif 'SEP' in surrogate_raw:
            db_surrogate = 'SEP'
        elif surrogate_raw == 'BANKING':
            db_surrogate = 'BANKING'
        elif surrogate_raw == 'GST':
            db_surrogate = 'GST'
        else:
            db_surrogate = surrogate_raw

        cibil = parse_num(row.get('MIN_CIBIL', ''))
        min_tenure = parse_num(row.get('Min_Tenure (Months)', ''))
        max_tenure = parse_num(row.get('Max_Tenure (Months)', ''))
        min_loan = parse_num(row.get('Min_LoanAmount', ''))
        max_loan = parse_num(row.get('Max_LoanAmount', ''))
        min_income = parse_num(row.get('Min_Income', ''))
        min_age = parse_num(row.get('Min_Age', ''))
        max_age = parse_num(row.get('Max_Age', ''))
        ltv = parse_ltv(row.get('LTV', ''))

        banks = [lender]
        # User requirement: "for lap here bajaj prime is bajaj finance only"
        if lender == 'Bajaj Finance' and db_product == 'LOAN AGAINST PROPERTY':
            banks.append('Bajaj Prime')

        for b in banks:
            for emp in db_emp_types:
                sql = f"""UPDATE eligibility_conditions 
SET cibil_min = {cibil}, min_income = {min_income}, min_age = {min_age}, max_age = {max_age}, 
    min_tenure = {min_tenure}, max_tenure = {max_tenure}, min_loan_amount = {min_loan}, max_loan_amount = {max_loan}, ltv_allowed = {ltv} 
WHERE bank_name = '{b}' AND loan_type = '{db_product}' AND employment_type = '{emp}' AND surrogate = '{db_surrogate}';"""
                sql_statements.append(sql)

unique_sqls = list(dict.fromkeys(sql_statements))
with open('/Users/manishmehta/Documents/PRYME-BACKEND-PROD/scratch/generate_v37.sql', 'w') as f:
    f.write("ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS min_tenure INTEGER;\n")
    f.write("ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS max_tenure INTEGER;\n")
    f.write("ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS min_loan_amount NUMERIC(15,2);\n")
    f.write("ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS max_loan_amount NUMERIC(15,2);\n\n")
    for sql in unique_sqls:
        f.write(sql + "\n")
