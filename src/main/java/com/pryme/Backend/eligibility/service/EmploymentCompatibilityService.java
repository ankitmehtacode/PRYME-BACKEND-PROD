package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.policy.model.EmploymentType;
import org.springframework.stereotype.Service;

/**
 * 🤝 Reusable domain matcher validating employment compatibility of loan products.
 */
@Service
public class EmploymentCompatibilityService {

    public boolean isProductAllowedForEmploymentType(String productCode, String lenderName, EmploymentType empType) {
        if (empType == null) {
            return true;
        }

        String code = productCode != null ? productCode.toUpperCase() : "";
        String lender = lenderName != null ? lenderName.toUpperCase() : "";

        switch (empType) {
            case SALARIED:
                if (code.endsWith("-0002") || code.endsWith("-0003") || code.endsWith("-SEP") || code.endsWith("-SENP")) {
                    return false;
                }
                break;
            case SENP:
                if (code.endsWith("-0001") || code.endsWith("-SAL") || code.endsWith("-SALARIED")) {
                    return false;
                }
                break;
            case SEP:
                if (lender.contains("BAJAJ") || lender.contains("L&T") || code.startsWith("BAJAJ-")
                        || code.startsWith("LT-") || code.startsWith("LT_")) {
                    if (code.endsWith("-0001") || code.endsWith("-SAL") || code.endsWith("-SALARIED")) {
                        return false;
                    }
                } else {
                    if (code.endsWith("-0002") || code.endsWith("-0003") || code.endsWith("-SEP")
                            || code.endsWith("-SENP")) {
                        return false;
                    }
                }
                break;
            case SELF_EMPLOYED:
                if (code.endsWith("-0001") || code.endsWith("-SAL") || code.endsWith("-SALARIED")) {
                    return false;
                }
                break;
        }

        return true;
    }
}
