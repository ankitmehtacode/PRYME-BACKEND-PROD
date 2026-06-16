import json
import os

transcript_path = "/Users/manishmehta/.gemini/antigravity-ide/brain/fab29e67-b8c3-4ebb-b36b-678367486f99/.system_generated/logs/transcript.jsonl"
output_path = "/Users/manishmehta/.gemini/antigravity-ide/brain/fab29e67-b8c3-4ebb-b36b-678367486f99/extracted_sheets.md"

steps_to_extract = {
    769: "Updated Master Sheet",
    944: "Updated FOIR Sheet",
    35: "LAP LTV Sheet (Part 1)",
    41: "LAP LTV Sheet (Part 2)"
}

extracted_data = {}

with open(transcript_path, "r", encoding="utf-8") as f:
    for line in f:
        try:
            data = json.loads(line)
            step_idx = data.get("step_index")
            if step_idx in steps_to_extract:
                content = data.get("content", "")
                # Clean up <USER_REQUEST> and </USER_REQUEST> tags and any metadata trailing
                if "<USER_REQUEST>" in content:
                    content = content.split("<USER_REQUEST>")[1]
                if "</USER_REQUEST>" in content:
                    content = content.split("</USER_REQUEST>")[0]
                content = content.strip()
                extracted_data[step_idx] = content
        except Exception as e:
            pass

markdown_content = """# Extracted Policy Sheets

This document contains the raw sheets provided in previous steps of the conversation, compiled for easy matching and verification.

"""

for step_idx, title in sorted(steps_to_extract.items()):
    content = extracted_data.get(step_idx, "*Not found in logs*")
    markdown_content += f"## {title} (Step {step_idx})\n\n```text\n{content}\n```\n\n---\n\n"

with open(output_path, "w", encoding="utf-8") as out_f:
    out_f.write(markdown_content)

print("Markdown sheets compiled successfully to:", output_path)
