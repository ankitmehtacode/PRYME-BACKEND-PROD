import re

with open('/Users/manishmehta/Documents/PRYME-BACKEND-PROD/src/main/resources/db/migration/V57__reload_curated_eligibility_conditions.sql', 'r') as f:
    content = f.read()

# Replace INSERT columns
content = content.replace(
    'min_age, max_age, min_tenure, max_tenure, min_loan_amount, max_loan_amount,',
    'min_age, max_age,'
)
content = content.replace(
    'v.min_age, v.max_age, v.min_tenure, v.max_tenure, v.min_loan_amount, v.max_loan_amount,',
    'v.min_age, v.max_age,'
)
content = content.replace(
    'min_age, max_age, min_tenure, max_tenure, min_loan_amount, max_loan_amount)',
    'min_age, max_age)'
)

# Strip last 4 elements from each VALUES tuple
def fix_values(match):
    # A line looks like: ('ABFL-HL-0001', 'SALARIED_SEP', 'NIP', 650, 30000.0, 22, 62, 36, 300, 3500000.0, 75000000.0),
    line = match.group(0)
    # find the tuple contents
    m = re.match(r"(\s*\()(.*)(\)(,)?\s*)", line)
    if not m:
        return line
    prefix, inner, suffix, comma = m.groups()
    parts = inner.split(',')
    # Keep only the first 7 columns: product_code, emp_type, surrogate, cibil, min_inc, min_age, max_age
    new_inner = ','.join(parts[:7])
    return prefix + new_inner + suffix

# The values section contains lines starting with (
# Let's just use a regex for the tuples
content = re.sub(r"^\s*\('.*?\).*$", fix_values, content, flags=re.MULTILINE)

# Replace the ON CONFLICT DO UPDATE SET section
update_part = """  min_age          = EXCLUDED.min_age,
  max_age          = EXCLUDED.max_age,
  min_tenure       = EXCLUDED.min_tenure,
  max_tenure       = EXCLUDED.max_tenure,
  min_loan_amount  = EXCLUDED.min_loan_amount,
  max_loan_amount  = EXCLUDED.max_loan_amount,
  is_active        = true,"""
new_update_part = """  min_age          = EXCLUDED.min_age,
  max_age          = EXCLUDED.max_age,
  is_active        = true,"""

content = content.replace(update_part, new_update_part)

with open('/Users/manishmehta/Documents/PRYME-BACKEND-PROD/src/main/resources/db/migration/V57__reload_curated_eligibility_conditions.sql', 'w') as f:
    f.write(content)

print("Done")
