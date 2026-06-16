import json

log_path = "/Users/manishmehta/.gemini/antigravity-ide/brain/068f305d-0186-4e93-bc4b-d425ac7e4acd/.system_generated/logs/transcript.jsonl"
with open(log_path, 'r') as f:
    for line in f:
        data = json.loads(line)
        if data.get("step_index") == 382:
            content = data.get("content")
            # print it in raw format
            print(content)
            break
