file_path = "src/main/java/com/pryme/Backend/eligibility/audit/certification/ReplayService.java"
with open(file_path, "r") as f:
    content = f.read()

# Replace the LTV comparison logic in ReplayService
old_logic = """                if (isAsPerMatrix || isBlank) {
                    expectedLtv = lowLtvSheetVal;
                } else {
                    expectedLtv = parsedLtv != null ? parsedLtv : BigDecimal.ZERO;
                }"""

new_logic = """                if (isAsPerMatrix || isBlank) {
                    expectedLtv = targetResult != null ? targetResult.ltv() : BigDecimal.ZERO;
                } else {
                    expectedLtv = parsedLtv != null ? parsedLtv : BigDecimal.ZERO;
                }"""

if old_logic in content:
    content = content.replace(old_logic, new_logic)
    with open(file_path, "w") as f:
        f.write(content)
    print("Updated LTV logic v2 in ReplayService")
else:
    print("Old LTV logic v2 not found in ReplayService!")
