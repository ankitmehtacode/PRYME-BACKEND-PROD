import os
import json

# Can we fetch from DB directly to see what the active bundle contains?
import psycopg2
conn = psycopg2.connect("postgres://neondb_owner:npg_VbzCd0Anf8oZ@ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require")
cur = conn.cursor()
cur.execute("SELECT eligibility_rules FROM policy_certification WHERE is_active = true ORDER BY created_at DESC LIMIT 1")
res = cur.fetchone()
if res and res[0]:
    rules = res[0]
    # rules is JSON. Let's just print the distinct lender_name and product_name
    if isinstance(rules, str):
        rules = json.loads(rules)
    lenders = set()
    prods = set()
    for row in rules:
        lenders.add(row.get('lenderName'))
        prods.add(row.get('productName'))
    print("Lenders in active bundle:", lenders)
    print("Products in active bundle:", prods)
else:
    print("No active bundle found")
