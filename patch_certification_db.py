import re

file_path = "src/main/java/com/pryme/Backend/eligibility/audit/certification/CertificationService.java"
with open(file_path, "r") as f:
    content = f.read()

# Replace DB_CROSS_REFERENCE comparison logic
old_logic = """                    String condSurr = cond.getSurrogate() != null ? cond.getSurrogate().trim() : "";
                    String rowSurr = row.surrogate() != null ? row.surrogate().trim() : "";
                    if (cond.getProductCode().startsWith(getProductCodePrefix(row))
                            && cond.getEmploymentType() != null && cond.getEmploymentType().equalsIgnoreCase(row.employmentType())
                            && condSurr.equalsIgnoreCase(rowSurr)) {"""

new_logic = """                    String condSurr = normalizer.normalizeSurrogate(cond.getSurrogate());
                    String rowSurr = normalizer.normalizeSurrogate(row.surrogate());
                    String condEmp = normalizer.normalizeEmploymentType(cond.getEmploymentType());
                    String rowEmp = normalizer.normalizeEmploymentType(row.employmentType());
                    if (cond.getProductCode().startsWith(getProductCodePrefix(row))
                            && condEmp.equalsIgnoreCase(rowEmp)
                            && condSurr.equalsIgnoreCase(rowSurr)) {"""

content = content.replace(old_logic, new_logic)

# Replace parseLtvAllowed and add SemanticValue concept?
# The user said: "Update the replay logic so matrix/runtime-derived fields are validated according to their intended semantics rather than through naive numeric equality."
# "Only if the business specification explicitly defines a default (for example, an 80% LTV default) should the certification expectation adopt that default."
# In CertificationService, expectedLtv is used for reporting. 

with open(file_path, "w") as f:
    f.write(content)
print("Updated DB_CROSS_REFERENCE logic in CertificationService.java")
