import json

log_path = "/Users/manishmehta/.gemini/antigravity-ide/brain/068f305d-0186-4e93-bc4b-d425ac7e4acd/.system_generated/logs/transcript.jsonl"
lines_to_print = []

with open(log_path, 'r') as f:
    for line in f:
        data = json.loads(line)
        content = data.get("content", "")
        if "Product_Name" in content and "Min_Loan_Amount" in content and ("HL" in content or "LAP" in content):
            # Extract the raw user request table
            for row in content.split("\n"):
                if row.strip().startswith("HL") or row.strip().startswith("LAP") or row.strip().startswith("Product_Name"):
                    lines_to_print.append(row)
            break

for line in lines_to_print:
    print(line)
