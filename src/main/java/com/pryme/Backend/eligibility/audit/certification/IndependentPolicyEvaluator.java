package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import com.pryme.Backend.loanproduct.entity.ProductRoiMatrix;
import com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndependentPolicyEvaluator {

    private final CentralizedNormalizer normalizer;
    private final ProductRoiMatrixRepository roiMatrixRepository;

    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int tenureMonths) {
        int effectiveTenure = tenureMonths > 0 ? tenureMonths : 12;
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(effectiveTenure), 2, RoundingMode.HALF_UP);
        }
        MathContext mc = MathContext.DECIMAL128;
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), mc);
        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate, mc).pow(effectiveTenure, mc);
        BigDecimal numerator = monthlyRate.multiply(onePlusRToN, mc);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE, mc);
        return principal.multiply(numerator.divide(denominator, mc), mc).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal lookupFoir(List<WorkbookModels.FoirRow> foirRows, String lenderName, String surrogate, String employmentType, BigDecimal monthlyIncome) {
        String normLender = normalizer.normalizeLender(lenderName);
        String normEmp = normalizer.normalizeEmploymentType(employmentType);
        String normSurrogate = normalizer.normalizeSurrogate(surrogate);
        
        for (var row : foirRows) {
            if (normalizer.normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && normalizer.normalizeSurrogate(row.surrogate()).equalsIgnoreCase(normSurrogate)
                && normalizer.normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal lower = row.lowerSalary() != null ? row.lowerSalary() : BigDecimal.ZERO;
                BigDecimal upper = row.upperSalary() != null ? row.upperSalary() : new BigDecimal("999999999");
                
                if (monthlyIncome.compareTo(lower) >= 0 && monthlyIncome.compareTo(upper) <= 0) {
                    return row.foir();
                }
            }
        }
        return new BigDecimal("0.65"); // fallback
    }

    public BigDecimal calculateProcessingFee(List<WorkbookModels.PfRow> pfRows, String lenderName, String loanType, String employmentType, BigDecimal loanAmount) {
        String normLender = normalizer.normalizeLender(lenderName);
        String normEmp = normalizer.normalizeEmploymentType(employmentType);
        String normLoanType = normalizer.normalizeLoanType(loanType);
        for (var row : pfRows) {
            if (normalizer.normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && normalizer.normalizeLoanType(row.loanType()).equalsIgnoreCase(normLoanType)
                && normalizer.normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                
                if (loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0) {
                    BigDecimal pfVal = row.pf();
                    BigDecimal taxRate = row.tax() != null ? row.tax() : new BigDecimal("0.18");
                    BigDecimal fee;
                    if (pfVal.compareTo(BigDecimal.ONE) < 0) {
                        fee = loanAmount.multiply(pfVal);
                    } else {
                        fee = pfVal;
                    }
                    BigDecimal gst = fee.multiply(taxRate);
                    return fee.add(gst).setScale(2, RoundingMode.HALF_UP);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal lookupLoginFee(List<WorkbookModels.LoginFeeRow> loginFeeRows, String lenderName, String loanType, String employmentType, BigDecimal loanAmount) {
        String normLender = normalizer.normalizeLender(lenderName);
        String normEmp = normalizer.normalizeEmploymentType(employmentType);
        String normLoanType = normalizer.normalizeLoanType(loanType);
        for (var row : loginFeeRows) {
            if (normalizer.normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && normalizer.normalizeLoanType(row.loanType()).equalsIgnoreCase(normLoanType)
                && normalizer.normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                
                if (loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0) {
                    return row.loginFees() != null ? row.loginFees() : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal lookupHlLtv(List<WorkbookModels.HlLtvRow> hlLtvRows, String propertyType, BigDecimal loanAmount) {
        String normProp = propertyType != null ? propertyType.trim() : "";
        boolean isPlot = normProp.toLowerCase().contains("plot");
        String matchProp = isPlot ? "Plot" : "Ready Built Property";
        
        for (var row : hlLtvRows) {
            if (row.propertyType().equalsIgnoreCase(matchProp)) {
                BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                if (loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0) {
                    return row.ltv();
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal lookupLapLtv(List<WorkbookModels.LapLtvRow> lapLtvRows, String lenderName, String propertyCategory, String propertySubtype) {
        String normLender = normalizer.normalizeLender(lenderName);
        String normCat = propertyCategory != null ? propertyCategory.trim() : "";
        String normSub = propertySubtype != null ? propertySubtype.trim() : "";
        
        for (var row : lapLtvRows) {
            if (normalizer.normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && row.propertyCategory().equalsIgnoreCase(normCat)
                && row.propertySubtype().equalsIgnoreCase(normSub)) {
                
                String val = row.ltvValue();
                if (val == null || val.equalsIgnoreCase("Negative")) {
                    return BigDecimal.ZERO;
                }
                try {
                    return new BigDecimal(val);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal resolveLapLtvFromRequest(List<WorkbookModels.LapLtvRow> lapLtvRows, String lenderName, String propertyType, String propertyCategory, String businessPropertyCategory) {
        String pType = propertyType != null ? propertyType.toUpperCase() : "FLAT";
        String cat = propertyCategory != null ? propertyCategory.toUpperCase() : "RESIDENTIAL";
        String bCat = businessPropertyCategory != null ? businessPropertyCategory.toUpperCase() : "";

        String targetCategory = "Residential";
        String targetSubtype = "Flat/Apartment/House";

        if (pType.equals("PLOT") || pType.equals("LAND")) {
            targetSubtype = "Plot";
            if (cat.contains("COMMERCIAL") || bCat.contains("COMMERCIAL")) {
                targetCategory = "Commercial";
            } else if (cat.contains("INDUSTRIAL") || bCat.contains("INDUSTRIAL")) {
                targetCategory = "Commercial";
            } else {
                targetCategory = "Residential";
            }
        } else {
            if (pType.equals("HOSPITAL")) {
                targetCategory = "Commercial";
                targetSubtype = "Hospital";
            } else if (pType.equals("HOSTEL")) {
                targetCategory = "Commercial";
                targetSubtype = "Hostel";
            } else if (pType.equals("RESTAURANTS") || pType.equals("RESTAURANT")) {
                targetCategory = "Commercial";
                targetSubtype = "Restaurent";
            } else if (pType.equals("HOTEL")) {
                targetCategory = "Commercial";
                targetSubtype = "Hotel";
            } else if (pType.equals("MARRIAGE_GARDEN")) {
                targetCategory = "Commercial";
                targetSubtype = "Marriage_Garden";
            } else if (cat.contains("COMMERCIAL") || bCat.contains("COMMERCIAL") || cat.contains("INDUSTRIAL") || bCat.contains("INDUSTRIAL")) {
                targetCategory = "Commercial";
                targetSubtype = "Restaurent";
            }
        }
        return lookupLapLtv(lapLtvRows, lenderName, targetCategory, targetSubtype);
    }

    public BigDecimal resolveDatabaseRoi(Long productId, String employmentType, BigDecimal loanAmount, int cibil, boolean isNtc, BigDecimal defaultRoi) {
        if (productId == null) {
            return defaultRoi;
        }
        List<ProductRoiMatrix> matrixRows = roiMatrixRepository.findByProductId(productId);
        if (matrixRows.isEmpty()) {
            return defaultRoi;
        }

        for (ProductRoiMatrix row : matrixRows) {
            if (row.isNtc() != isNtc) {
                if (row.isNtc() && !isNtc) continue;
                if (!row.isNtc() && isNtc) continue;
            }

            String rowEmpType = row.getEmploymentType();
            if (rowEmpType != null) {
                boolean match = false;
                if (rowEmpType.equalsIgnoreCase("SALARIED_SEP")) {
                    match = employmentType.equalsIgnoreCase("Salaried") || employmentType.equalsIgnoreCase("SEP/SENP");
                } else if (rowEmpType.equalsIgnoreCase("SEP_SENP") 
                        || rowEmpType.equalsIgnoreCase("SENP") 
                        || rowEmpType.equalsIgnoreCase("SEP")) {
                    match = employmentType.equalsIgnoreCase("Self Employed Professional") 
                            || employmentType.equalsIgnoreCase("Self Employed Non Professional")
                            || employmentType.equalsIgnoreCase("SEP/SENP");
                } else {
                    match = rowEmpType.equalsIgnoreCase(employmentType);
                }
                if (!match) continue;
            }

            BigDecimal minAmt = row.getMinLoanAmount() != null ? row.getMinLoanAmount() : BigDecimal.ZERO;
            BigDecimal maxAmt = row.getMaxLoanAmount() != null ? row.getMaxLoanAmount() : new BigDecimal("999999999");
            if (loanAmount.compareTo(minAmt) < 0 || loanAmount.compareTo(maxAmt) > 0) {
                continue;
            }

            Integer minCibil = row.getMinCibil();
            Integer maxCibil = row.getMaxCibil();
            if (minCibil != null && cibil < minCibil) continue;
            if (maxCibil != null && cibil > maxCibil) continue;

            return row.getRoi();
        }
        return defaultRoi;
    }
}
