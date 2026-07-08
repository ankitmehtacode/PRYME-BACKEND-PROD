import json

transcript_path = '/Users/manishmehta/.gemini/antigravity-ide/brain/3fda22ad-698c-46c3-a0d7-2f435eb18a64/.system_generated/logs/transcript_full.jsonl'

blocks = []
with open(transcript_path, 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get('type') == 'USER_INPUT':
            content = data.get('content', '')
            if 'Product_Name' in content or 'Foreclosure Charges' in content or 'Admin Fee' in content:
                blocks.append(content)

with open('user_all_tsvs.txt', 'w') as out:
    for i, b in enumerate(blocks):
        out.write(f"--- BLOCK {i} ---\n")
        out.write(b)
        out.write("\n\n")
