file_path = "src/main/java/com/pryme/Backend/eligibility/service/CentralizedNormalizer.java"
with open(file_path, "r") as f:
    content = f.read()

# Fix ICICI HFC in normalizeLender
old_icici = '        } else if (clean.contains("icici")) {\n            normalized = "ICICI Bank";'
new_icici = '        } else if (clean.contains("icici hfc") || clean.contains("icicihfc")) {\n            normalized = "ICICI HFC";\n        } else if (clean.contains("icici")) {\n            normalized = "ICICI Bank";'

if old_icici in content:
    content = content.replace(old_icici, new_icici)
    with open(file_path, "w") as f:
        f.write(content)
    print("Updated ICICI HFC in normalizeLender")
else:
    print("Old ICICI logic not found in normalizeLender!")
