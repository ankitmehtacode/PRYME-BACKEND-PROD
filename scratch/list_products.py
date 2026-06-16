import psycopg2

db_url = "postgresql://neondb_owner:npg_VbzCd0Anf8oZ@ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb"
try:
    conn = psycopg2.connect(db_url)
    cur = conn.cursor()
    cur.execute("SELECT id, product_code, product_name, lender_name, loan_type FROM loan_products ORDER BY lender_name, loan_type, product_code;")
    rows = cur.fetchall()
    print(f"{'ID':<5} | {'Product Code':<20} | {'Product Name':<45} | {'Lender':<25} | {'Type':<6}")
    print("-" * 110)
    for r in rows:
        print(f"{r[0]:<5} | {r[1]:<20} | {r[2]:<45} | {r[3]:<25} | {r[4]:<6}")
    cur.close()
    conn.close()
except Exception as e:
    print("Error:", e)
