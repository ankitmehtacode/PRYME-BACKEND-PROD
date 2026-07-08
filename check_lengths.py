import csv

csv_file = '/Users/manishmehta/Downloads/Eligibility_Product_Master_Curated_Final (1).csv'

with open(csv_file, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        p_code = row['product_code'].strip()
        if len(p_code) > 20:
            print(f"Product Code > 20 chars: '{p_code}' (len {len(p_code)})")
