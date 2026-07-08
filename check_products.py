import csv
import psycopg2

csv_file = '/Users/manishmehta/Downloads/Eligibility_Product_Master_Curated_Final (1).csv'

csv_products = set()
with open(csv_file, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    for row in reader:
        csv_products.add(row['product_code'].strip())

conn = psycopg2.connect("dbname=pryme_db user=manishmehta host=localhost")
cur = conn.cursor()
cur.execute("SELECT product_code FROM loan_products")
db_products = set(r[0] for r in cur.fetchall())

missing_in_db = csv_products - db_products
missing_in_csv = db_products - csv_products

print(f"Products in CSV not in DB ({len(missing_in_db)}): {sorted(list(missing_in_db))[:10]}...")
print(f"Products in DB not in CSV ({len(missing_in_csv)}): {sorted(list(missing_in_csv))[:10]}...")

