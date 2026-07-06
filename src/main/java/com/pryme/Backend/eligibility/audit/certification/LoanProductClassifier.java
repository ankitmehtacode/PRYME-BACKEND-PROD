package com.pryme.Backend.eligibility.audit.certification;

import com.pryme.Backend.eligibility.service.CentralizedNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanProductClassifier {

    private final CentralizedNormalizer normalizer;

    public enum LoanCategory {
        HL,
        LAP,
        PL,
        BL,
        LAS,
        UNKNOWN
    }

    public LoanCategory classify(String productName, String loanType) {
        String cleanProd = productName != null ? productName.trim().toUpperCase() : "";
        if (cleanProd.contains("HL") || cleanProd.contains("HOME")) {
            return LoanCategory.HL;
        }
        if (cleanProd.contains("LAP") || cleanProd.contains("PROPERTY")) {
            return LoanCategory.LAP;
        }

        String normLoanType = normalizer.normalizeLoanType(loanType);
        if ("HL".equalsIgnoreCase(normLoanType)) {
            return LoanCategory.HL;
        }
        if ("LAP".equalsIgnoreCase(normLoanType)) {
            return LoanCategory.LAP;
        }

        // Fallback checks for string matching
        String cleanType = loanType != null ? loanType.trim().toUpperCase() : "";

        if (cleanProd.contains("HL") || cleanProd.contains("HOME") || cleanType.contains("HL") || cleanType.contains("HOME")) {
            return LoanCategory.HL;
        }
        if (cleanProd.contains("LAP") || cleanProd.contains("PROPERTY") || cleanType.contains("LAP") || cleanType.contains("PROPERTY")) {
            return LoanCategory.LAP;
        }

        return LoanCategory.UNKNOWN;
    }
}
