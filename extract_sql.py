import json

with open('/Users/manishmehta/.gemini/antigravity-ide/brain/3fda22ad-698c-46c3-a0d7-2f435eb18a64/.system_generated/logs/transcript_full.jsonl', 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get('type') == 'USER_INPUT' and 'V51__reload_curated_eligibility_conditions.sql' in data.get('content', ''):
            content = data['content']
            # extract everything from -- V51... down to the end before </USER_REQUEST>
            # Actually the content starts with the user request.
            # Let's find the INSERT statement
            start = content.find('INSERT INTO eligibility_conditions')
            if start != -1:
                sql_part = content[start:content.find('</USER_REQUEST>')]
                with open('/Users/manishmehta/Documents/PRYME-BACKEND-PROD/extracted.sql', 'w') as out:
                    out.write(sql_part)
                print("Extracted SQL")
                break
