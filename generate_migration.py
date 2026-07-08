import csv

csv_file = '/Users/manishmehta/Downloads/Eligibility_Product_Master_Curated_Final (1).csv'
out_file = 'src/main/resources/db/migration/V40__load_curated_eligibility_master.sql'

lender_mapping = {
    'HDFC': ('HDFC Bank', 1),
    'HDFC Bank': ('HDFC Bank', 1),
    'SBI': ('SBI', 102),
    'State Bank of India': ('SBI', 102),
    'ICICI Bank': ('ICICI Bank', 105),
    'L&T Finance': ('L&T Finance', 101),
    'Bandhan Bank': ('Bandhan Bank', 200),
    'Aditya Birla Finance Limited': ('Aditya Birla Finance Limited', 201),
    'Bank of Baroda': ('Bank of Baroda', 202),
    'Bajaj Finance': ('Bajaj Finance', 203),
    'ICICI HFC': ('ICICI HFC', 204),
    'YES BANK': ('YES BANK', 205),
    'Yes Bank': ('YES BANK', 205),
    'JIO Finance': ('JIO Finance', 206),
    'IDBI': ('IDBI', 207),
    'IDBI Bank': ('IDBI', 207),
    'Tata Capital': ('Tata Capital', 208),
    'TATA Capital': ('Tata Capital', 208),
    'IDFC': ('IDFC', 209),
    'IDFC Bank': ('IDFC', 209),
    'Indus Ind Bank': ('Indus Ind Bank', 210)
}

product_limits = {}
eligibility_rows = []

with open(csv_file, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        surrogate = row['surrogate'].strip()
        
        if surrogate.lower() == 'low ltv' or surrogate.lower() == 'low_ltv':
            continue
            
        p_code = row['product_code'].strip()
        b_name = row['bank_name'].strip()
        l_type = row['loan_type'].strip()
        if l_type == 'Secured': l_type = 'LAP'
        if l_type == 'Unsecured': l_type = 'BL'
        if l_type == 'Home Loan': l_type = 'HL'
        
        cibil = row.get('cibil_min', '650').strip() or '650'
        min_tenure = row.get('min_tenure_months', '12').strip() or '12'
        max_tenure = row.get('max_tenure_months', '360').strip() or '360'
        min_loan = row.get('min_loan_amount', '100000').strip() or '100000'
        max_loan = row.get('max_loan_amount', '99999999').strip() or '99999999'
        
        mapped_name, mapped_id = lender_mapping.get(b_name, (b_name, 999))
        
        if p_code not in product_limits:
            product_limits[p_code] = {
                'lender_name': mapped_name,
                'lender_id': mapped_id,
                'loan_type': l_type,
                'min_cibil': cibil,
                'min_tenure': min_tenure,
                'max_tenure': max_tenure,
                'min_loan': min_loan,
                'max_loan': max_loan
            }
            
        if surrogate.lower() == 'banking': surrogate = 'BANKING'
        if surrogate.lower() == 'gst': surrogate = 'GST'
        if surrogate.lower() == 'nip': surrogate = 'NIP'
        if surrogate.lower() == 'sep': surrogate = 'SEP'
            
        eligibility_rows.append({
            'product_code': p_code,
            'employment_type': row['employment_type'].strip(),
            'surrogate': surrogate or 'NIP',
            'cibil_min': cibil,
            'min_income': row.get('min_income', '0').strip() or '0',
            'min_age': row.get('min_age', '21').strip() or '21',
            'max_age': row.get('max_age', '65').strip() or '65'
        })

with open(out_file, 'w') as f:
    f.write("-- V40__load_curated_eligibility_master.sql\n")
    f.write("-- Replaces existing eligibility rules with curated final Excel\n\n")
    
    f.write("-- 0. Expand column size for longer product codes and employment types\n")
    f.write("ALTER TABLE loan_products ALTER COLUMN product_code TYPE VARCHAR(100);\n")
    f.write("ALTER TABLE eligibility_conditions ALTER COLUMN product_code TYPE VARCHAR(100);\n")
    f.write("ALTER TABLE eligibility_conditions ALTER COLUMN employment_type TYPE VARCHAR(100);\n\n")
    
    f.write("-- 1. Upsert Loan Products\n")
    for p_code, limits in product_limits.items():
        p_name = f"{limits['lender_name']} {limits['loan_type']} Program"
        f.write(f"""INSERT INTO loan_products 
    (product_code, product_name, loan_type, lender_id, lender_name, interest_type, 
     min_cibil, max_cibil, roi, min_tenure_months, max_tenure_months, min_loan_amount, max_loan_amount, is_active)
VALUES 
    ('{p_code}', '{p_name}', '{limits['loan_type']}', {limits['lender_id']}, '{limits['lender_name']}', 'Floating',
     {limits['min_cibil']}, 900, 10.0, {limits['min_tenure']}, {limits['max_tenure']}, {limits['min_loan']}, {limits['max_loan']}, true)
ON CONFLICT (product_code) DO UPDATE SET
    min_cibil = EXCLUDED.min_cibil,
    min_tenure_months = EXCLUDED.min_tenure_months,
    max_tenure_months = EXCLUDED.max_tenure_months,
    min_loan_amount = EXCLUDED.min_loan_amount,
    max_loan_amount = EXCLUDED.max_loan_amount;
""")

    f.write("\n-- 2. Clear old eligibility conditions\n")
    f.write("DELETE FROM eligibility_conditions;\n\n")
    
    f.write("-- 3. Insert curated eligibility conditions\n")
    for r in eligibility_rows:
        f.write(f"""INSERT INTO eligibility_conditions
    (product_code, product_id, employment_type, surrogate, property_type, negative_property, negative_employer_type, negative_salary_mode,
     cibil_min, min_income, min_age, max_age)
VALUES
    ('{r['product_code']}', (SELECT id FROM loan_products WHERE product_code = '{r['product_code']}'), '{r['employment_type']}', '{r['surrogate']}', 'Any', 'false', 'false', 'false',
     {r['cibil_min']}, {r['min_income']}, {r['min_age']}, {r['max_age']});
""")

print("Migration V40 regenerated.")
