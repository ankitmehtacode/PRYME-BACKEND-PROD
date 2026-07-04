package com.pryme.Backend.eligibility.audit.certification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CertificationCalculator {

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
        String normLender = normalizeLender(lenderName);
        String normEmp = normalizeEmploymentType(employmentType);
        String normSurrogate = surrogate != null ? surrogate.trim().toUpperCase() : "NIP";
        
        for (var row : foirRows) {
            if (normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && row.surrogate().trim().toUpperCase().equalsIgnoreCase(normSurrogate)
                && normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal lower = row.lowerSalary() != null ? row.lowerSalary() : BigDecimal.ZERO;
                BigDecimal upper = row.upperSalary() != null ? row.upperSalary() : new BigDecimal("999999999");
                
                if (monthlyIncome.compareTo(lower) >= 0 && monthlyIncome.compareTo(upper) <= 0) {
                    return row.foir();
                }
            }
        }
        return new BigDecimal("0.65"); // fallback
    }

    public BigDecimal calculateProcessingFee(List<WorkbookModels.PfRow> pfRows, String lenderName, String employmentType, BigDecimal loanAmount) {
        String normLender = normalizeLender(lenderName);
        String normEmp = normalizeEmploymentType(employmentType);
        for (var row : pfRows) {
            if (normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
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

    public BigDecimal lookupLoginFee(List<WorkbookModels.LoginFeeRow> loginFeeRows, String lenderName, String employmentType, BigDecimal loanAmount) {
        String normLender = normalizeLender(lenderName);
        String normEmp = normalizeEmploymentType(employmentType);
        for (var row : loginFeeRows) {
            if (normalizeLender(row.lenderName()).equalsIgnoreCase(normLender)
                && normalizeEmploymentType(row.employmentType()).equalsIgnoreCase(normEmp)) {
                
                BigDecimal minAmt = row.minLoanAmount() != null ? row.minLoanAmount() : BigDecimal.ZERO;
                BigDecimal maxAmt = row.maxLoanAmount() != null ? row.maxLoanAmount() : new BigDecimal("999999999");
                
                if (loanAmount.compareTo(minAmt) >= 0 && loanAmount.compareTo(maxAmt) <= 0) {
                    return row.loginFees() != null ? row.loginFees() : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public String normalizeLender(String name) {
        if (name == null) return "";
        String clean = name.trim().toLowerCase();
        if (clean.contains("l&t") || clean.contains("lt ") || clean.contains("ltfinance")) return "L&T Finance";
        if (clean.contains("icici")) return "ICICI Bank";
        if (clean.contains("bandhan")) return "Bandhan Bank";
        if (clean.contains("aditya") || clean.contains("abfl") || clean.contains("birla")) return "Aditya Birla Finance Limited";
        if (clean.contains("baroda") || clean.contains("bob")) return "Bank of Baroda";
        if (clean.contains("sbi") || clean.contains("state bank")) return "SBI";
        if (clean.contains("bajaj finance")) return "Bajaj Finance";
        if (clean.contains("bajaj prime")) return "Bajaj Prime";
        if (clean.contains("yes")) return "YES BANK";
        if (clean.contains("hdfc")) return "HDFC Bank";
        if (clean.contains("jio")) return "JIO Finance";
        if (clean.contains("idbi")) return "IDBI";
        if (clean.contains("tata")) return "Tata Capital";
        if (clean.contains("idfc")) return "IDFC";
        return name;
    }

    public String normalizeEmploymentType(String empType) {
        if (empType == null) return "";
        String clean = empType.trim().toLowerCase();
        if (clean.contains("salaried")) return "Salaried";
        if (clean.contains("non professional") || clean.contains("senp")) return "Self Employed Non Professional";
        if (clean.contains("professional") || clean.contains("sep")) return "Self Employed Professional";
        if (clean.contains("self employed")) return "Self Employed Professional/Self Employed Non Professional";
        return empType;
    }
}
