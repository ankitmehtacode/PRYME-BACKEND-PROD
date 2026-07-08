import json
import sys

transcript_path = '/Users/manishmehta/.gemini/antigravity-ide/brain/3fda22ad-698c-46c3-a0d7-2f435eb18a64/.system_generated/logs/transcript_full.jsonl'

tsv_content = None
with open(transcript_path, 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get('type') == 'USER_INPUT':
            content = data.get('content', '')
            if 'Admin Fee' in content and 'Foreclosure Charges' in content:
                tsv_content = content

if tsv_content:
    print(f"Found TSV of length {len(tsv_content)}")
    with open('user_tsv_full.txt', 'w') as out:
        out.write(tsv_content)
    sys.exit(0)
print("Not found")
