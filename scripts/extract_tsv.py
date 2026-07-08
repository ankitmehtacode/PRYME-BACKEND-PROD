import json
import sys

transcript_path = '/Users/manishmehta/.gemini/antigravity-ide/brain/3fda22ad-698c-46c3-a0d7-2f435eb18a64/.system_generated/logs/transcript_full.jsonl'

with open(transcript_path, 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get('type') == 'USER_INPUT':
            content = data.get('content', '')
            if 'Product_Name\tLoan_Type\tLender_Name\tInterest_Type\tProperty_Type\tNegative_Property' in content:
                print("FOUND TSV DATA, length:", len(content))
                with open('user_tsv_data.txt', 'w') as out:
                    out.write(content)
                sys.exit(0)
print("Not found")
