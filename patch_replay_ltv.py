file_path = "src/main/java/com/pryme/Backend/eligibility/audit/certification/ReplayService.java"
with open(file_path, "r") as f:
    content = f.read()

# Replace the LTV comparison logic in ReplayService
old_logic = """            } else {
                BigDecimal parsedLtv = parseLtvAllowed(row.ltv());
                expectedLtv = parsedLtv != null ? parsedLtv : BigDecimal.ZERO;

                if (targetResult != null) {
                    BigDecimal actualLtv = targetResult.ltv();
                    BigDecimal lowLtvSheetVal = BigDecimal.ZERO;
                    if ("HL".equalsIgnoreCase(row.productName())) {
                        lowLtvSheetVal = evaluator.lookupHlLtv(hlLtvRows, request.propertyType(), request.loanAmount());
                    } else {
                        lowLtvSheetVal = evaluator.resolveLapLtvFromRequest(
                                lapLtvRows,
                                row.lenderName(),
                                request.propertyType(),
                                request.propertyCategory(),
                                request.businessPropertyCategory()
                        );
                    }

                    if (actualLtv != null && actualLtv.compareTo(BigDecimal.ZERO) > 0 
                            && lowLtvSheetVal != null && lowLtvSheetVal.compareTo(BigDecimal.ZERO) > 0
                            && actualLtv.compareTo(lowLtvSheetVal) == 0 
                            && (parsedLtv == null || parsedLtv.compareTo(lowLtvSheetVal) != 0)) {
                        
                        deviations.add(new CertificationReportModels.FieldMismatch(
                                "LTV_ISOLATION",
                                parsedLtv != null ? parsedLtv.toString() : "NA",
                                actualLtv.toString(),
                                "ENGINE_LOGIC_MISMATCH: NIP/surrogate program consulted Low LTV fallbacks outside Low LTV cascade"
                        ));
                    }
                }
            }"""

new_logic = """            } else {
                boolean isAsPerMatrix = row.ltv() != null && row.ltv().toLowerCase().contains("as per");
                boolean isBlank = row.ltv() == null || row.ltv().trim().isEmpty() || row.ltv().equalsIgnoreCase("NA") || row.ltv().equalsIgnoreCase("Negative");

                BigDecimal parsedLtv = parseLtvAllowed(row.ltv());

                BigDecimal lowLtvSheetVal = BigDecimal.ZERO;
                if ("HL".equalsIgnoreCase(row.productName())) {
                    lowLtvSheetVal = evaluator.lookupHlLtv(hlLtvRows, request.propertyType(), request.loanAmount());
                } else {
                    lowLtvSheetVal = evaluator.resolveLapLtvFromRequest(
                            lapLtvRows,
                            row.lenderName(),
                            request.propertyType(),
                            request.propertyCategory(),
                            request.businessPropertyCategory()
                    );
                }

                if (isAsPerMatrix || isBlank) {
                    expectedLtv = lowLtvSheetVal;
                } else {
                    expectedLtv = parsedLtv != null ? parsedLtv : BigDecimal.ZERO;
                }

                if (targetResult != null) {
                    BigDecimal actualLtv = targetResult.ltv();
                    if (actualLtv != null && actualLtv.compareTo(BigDecimal.ZERO) > 0 
                            && lowLtvSheetVal != null && lowLtvSheetVal.compareTo(BigDecimal.ZERO) > 0
                            && actualLtv.compareTo(lowLtvSheetVal) == 0 
                            && (parsedLtv == null || parsedLtv.compareTo(lowLtvSheetVal) != 0)) {
                        
                        if (!isAsPerMatrix && !isBlank) {
                            deviations.add(new CertificationReportModels.FieldMismatch(
                                    "LTV_ISOLATION",
                                    parsedLtv != null ? parsedLtv.toString() : "NA",
                                    actualLtv.toString(),
                                    "ENGINE_LOGIC_MISMATCH: NIP/surrogate program consulted Low LTV fallbacks outside Low LTV cascade"
                            ));
                        }
                    }
                }
            }"""

if old_logic in content:
    content = content.replace(old_logic, new_logic)
    with open(file_path, "w") as f:
        f.write(content)
    print("Updated LTV parsing logic in ReplayService")
else:
    print("Old LTV logic not found in ReplayService!")
