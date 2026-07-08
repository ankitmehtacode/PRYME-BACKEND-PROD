file_path = "src/main/java/com/pryme/Backend/eligibility/service/CentralizedNormalizer.java"
with open(file_path, "r") as f:
    content = f.read()

# Fix ICICI HFC prefix
content = content.replace(
    'else if (cleanLender.contains("ICICI")) prefix = "ICICI";',
    'else if (cleanLender.contains("ICICI HFC") || cleanLender.contains("ICICIHFC")) prefix = "ICICIHFC";\n        else if (cleanLender.contains("ICICI")) prefix = "ICICI";'
)

with open(file_path, "w") as f:
    f.write(content)
print("Updated ICICI HFC prefix in normalizer")
