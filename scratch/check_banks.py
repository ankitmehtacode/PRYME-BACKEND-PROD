import psycopg2

db_url = "postgresql://neondb_owner:npg_VbzCd0Anf8oZ@ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb"
try:
    conn = psycopg2.connect(db_url)
    cur = conn.cursor()
    cur.execute("SELECT DISTINCT bank_name, product FROM eligibility_conditions;")
    for row in cur.fetchall():
        print(row)
    cur.close()
    conn.close()
except Exception as e:
    print("Error:", e)
