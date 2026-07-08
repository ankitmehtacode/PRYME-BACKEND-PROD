import csv

lender_map = {}
with open('db_lenders.txt', 'r') as f:
    for line in f:
        if '|' in line:
            name, lid = line.strip().split('|')
            lender_map[name.strip()] = int(lid.strip())

csv_file = '/Users/manishmehta/Downloads/Eligibility_Product_Master_Curated_Final (1).csv'
csv_lenders = set()
with open(csv_file, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        csv_lenders.add(row['bank_name'].strip())

for lender in csv_lenders:
    if lender not in lender_map:
        print(f"NEW LENDER: {lender}")
    else:
        print(f"EXISTING LENDER: {lender} -> {lender_map[lender]}")

