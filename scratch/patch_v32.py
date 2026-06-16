import re

file_path = "/Users/manishmehta/Documents/PRYME-BACKEND-PROD/src/main/resources/db/migration/V32__enrich_product_config_master.sql"

with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Replace Salaried checks
content = content.replace("employment_type='Salaried'", "employment_type IN ('Salaried', 'SALARIED_SEP')")

# 2. Replace Self Employed Professional / Non Professional checks
# Handle possible variations in spacing/quoting
pattern = r"employment_type\s+IN\s*\(\s*'Self Employed Professional'\s*,\s*'Self Employed Non Professional'\s*\)"
content = re.sub(pattern, "employment_type IN ('Self Employed Professional', 'Self Employed Non Professional', 'SEP_SENP', 'SENP', 'SEP', 'SEP/SENP')", content)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Patch applied successfully to V32__enrich_product_config_master.sql")
