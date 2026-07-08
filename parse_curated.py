import csv
import sys

csv_file = '/Users/manishmehta/Downloads/Eligibility_Product_Master_Curated_Final (1).csv'

product_limits = {}
eligibility_rows = []

try:
    with open(csv_file, 'r', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            p_code = row['product_code'].strip()
            
            # Record limits
            limits = {
                'min_cibil': row.get('cibil_min', '650') or '650',
                'min_tenure': row.get('min_tenure_months', '12') or '12',
                'max_tenure': row.get('max_tenure_months', '360') or '360',
                'min_loan': row.get('min_loan_amount', '100000') or '100000',
                'max_loan': row.get('max_loan_amount', '99999999') or '99999999'
            }
            if p_code in product_limits:
                if product_limits[p_code] != limits:
                    pass # We will just use the first one, but print if conflict
            else:
                product_limits[p_code] = limits
                
            eligibility_rows.append(row)

    print(f"Total rows: {len(eligibility_rows)}")
    print(f"Total unique products: {len(product_limits)}")
except Exception as e:
    print(e)
