package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.policy.model.EmploymentType;
import org.springframework.stereotype.Component;

@Component
public class CentralizedNormalizer {

    private final AliasRegistry aliasRegistry;

    public CentralizedNormalizer() {
        this.aliasRegistry = new AliasRegistry();
    }

    @org.springframework.beans.factory.annotation.Autowired
    public CentralizedNormalizer(AliasRegistry aliasRegistry) {
        this.aliasRegistry = aliasRegistry;
    }

    public String normalizeLender(String name) {
        if (name == null) return "";
        String resolved = aliasRegistry.resolveCanonical(name);
        String clean = resolved.trim().toLowerCase();
        
        String normalized;
        if (clean.contains("l&t") || clean.contains("lt ") || clean.contains("ltfinance") || clean.contains("l & t")) {
            normalized = "L&T Finance";
        } else if (clean.contains("icici")) {
            normalized = "ICICI Bank";
        } else if (clean.contains("bandhan")) {
            normalized = "Bandhan Bank";
        } else if (clean.contains("aditya") || clean.contains("abfl") || clean.contains("birla")) {
            normalized = "Aditya Birla Finance Limited";
        } else if (clean.contains("baroda") || clean.contains("bob")) {
            normalized = "Bank of Baroda";
        } else if (clean.contains("sbi") || clean.contains("state bank")) {
            normalized = "SBI";
        } else if (clean.contains("bajaj prime")) {
            normalized = "Bajaj Prime";
        } else if (clean.contains("bajaj")) {
            normalized = "Bajaj Finance";
        } else if (clean.contains("yes")) {
            normalized = "YES BANK";
        } else if (clean.contains("hdfc")) {
            normalized = "HDFC Bank";
        } else if (clean.contains("jio")) {
            normalized = "JIO Finance";
        } else if (clean.contains("idbi")) {
            normalized = "IDBI";
        } else if (clean.contains("tata")) {
            normalized = "Tata Capital";
        } else if (clean.contains("idfc")) {
            normalized = "IDFC";
        } else {
            normalized = resolved.trim();
        }
        return aliasRegistry.resolveCanonical(normalized);
    }

    public String getProductCodePrefix(String lenderName, String productName) {
        if (lenderName == null) return "";
        String normLender = normalizeLender(lenderName);
        String cleanLender = normLender.toUpperCase();
        String prodName = productName != null ? productName.trim() : "HL";
        String suffix = "HL".equalsIgnoreCase(prodName) ? "HL" : "LAP";
        String prefix;
        if (cleanLender.contains("L&T") || cleanLender.contains("L & T") || cleanLender.contains("LT ") || cleanLender.contains("LTFINANCE")) prefix = "LT";
        else if (cleanLender.contains("ICICI")) prefix = "ICICI";
        else if (cleanLender.contains("BANDHAN")) prefix = "BANDHAN";
        else if (cleanLender.contains("ADITYA") || cleanLender.contains("ABFL") || cleanLender.contains("BIRLA")) prefix = "ABFL";
        else if (cleanLender.contains("BARODA") || cleanLender.contains("BOB")) prefix = "BOB";
        else if (cleanLender.contains("SBI") || cleanLender.contains("STATE BANK")) prefix = "SBI";
        else if (cleanLender.contains("BAJAJ")) prefix = "BAJAJ";
        else if (cleanLender.contains("YES")) prefix = "YES";
        else if (cleanLender.contains("HDFC")) prefix = "HDFC";
        else if (cleanLender.contains("JIO")) prefix = "JIO";
        else if (cleanLender.contains("IDBI")) prefix = "IDBI";
        else if (cleanLender.contains("TATA")) prefix = "TATA";
        else if (cleanLender.contains("IDFC")) prefix = "IDFC";
        else prefix = normLender;
        return prefix + "-" + suffix;
    }

    public String normalizeEmploymentType(String empType) {
        if (empType == null) return "";
        String clean = empType.trim().toLowerCase();
        if (clean.contains("salaried")) return "Salaried";
        if (clean.contains("non professional") || clean.contains("senp")) return "Self Employed Non Professional";
        if (clean.contains("professional") || clean.contains("sep")) return "Self Employed Professional";
        if (clean.contains("self employed")) return "Self Employed Professional/Self Employed Non Professional";
        return empType.trim();
    }

    public EmploymentType normalizeToEnum(String rawEmpType) {
        if (rawEmpType == null) return null;
        String clean = rawEmpType.trim().toLowerCase();
        if (clean.contains("salaried")) {
            return EmploymentType.SALARIED;
        }
        if (clean.contains("non professional") || clean.contains("senp")) {
            return EmploymentType.SENP;
        }
        if (clean.contains("professional") || clean.contains("sep")) {
            return EmploymentType.SEP;
        }
        if (clean.contains("self employed")) {
            return EmploymentType.SELF_EMPLOYED;
        }
        return EmploymentType.SELF_EMPLOYED;
    }

    public String normalizeSurrogate(String surrogate) {
        if (surrogate == null) return "NIP";
        String clean = surrogate.replaceAll("[\\s_-]+", "").toUpperCase();
        if (clean.isEmpty()) return "NIP";
        if (clean.contains("LOWLTV")) return "LOW_LTV";
        return clean;
    }

    public String normalizeLoanType(String loanType) {
        if (loanType == null) return "HL";
        String clean = loanType.trim().toLowerCase();
        if (clean.contains("lap") || clean.contains("against property") || clean.contains("secured loan") || clean.contains("secured")) {
            return "LAP";
        }
        return "HL";
    }

    public String normalizeBusinessTypeKey(String raw) {
        if (raw == null) return "default";
        String clean = raw.trim().toLowerCase();
        if (clean.contains("manufactur")) return "manufacturing";
        if (clean.contains("trader") || clean.contains("retail") || clean.contains("shop")) return "trader";
        if (clean.contains("service") || clean.contains("profession")) return "service";
        if (clean.contains("wholesale") || clean.contains("distributor")) return "wholesale";
        return clean;
    }

    public String normalizePropertyCategory(String propType) {
        if (propType == null) return "RESIDENTIAL";
        String clean = propType.trim().toUpperCase();
        if (clean.contains("PLOT")) return "PLOT";
        if (clean.contains("COMMERCIAL") || clean.contains("SHOP")) return "COMMERCIAL";
        if (clean.contains("INDUSTRIAL") || clean.contains("FACTORIES") || clean.contains("WORKSHOP")) return "INDUSTRIAL";
        return "RESIDENTIAL";
    }

    public boolean matchRoiEmploymentType(String rowEmpType, String empTypeStr) {
        return matchRoiEmploymentType(rowEmpType, empTypeStr, null);
    }

    public boolean matchRoiEmploymentType(String rowEmpType, String empTypeStr, String lenderName) {
        if (rowEmpType == null) return true;
        if (empTypeStr == null) return false;

        EmploymentType rowEnum = normalizeToEnum(rowEmpType);
        EmploymentType empEnum = normalizeToEnum(empTypeStr);

        String r = rowEmpType.trim().toUpperCase();
        String a = empTypeStr.trim().toUpperCase();

        if (r.equals("SALARIED_SEP")) {
            return empEnum == EmploymentType.SALARIED || empEnum == EmploymentType.SEP || empEnum == EmploymentType.SELF_EMPLOYED;
        }
        if (r.equals("SEP_SENP") || r.equals("SEP/SENP") || r.equals("SELF EMPLOYED PROFESSIONAL/SELF EMPLOYED NON PROFESSIONAL")) {
            return empEnum == EmploymentType.SEP || empEnum == EmploymentType.SENP || empEnum == EmploymentType.SELF_EMPLOYED;
        }
        if (r.equals("SEP") || r.equals("SELF EMPLOYED PROFESSIONAL")) {
            return empEnum == EmploymentType.SEP || empEnum == EmploymentType.SELF_EMPLOYED;
        }
        if (r.equals("SENP") || r.equals("SELF EMPLOYED NON PROFESSIONAL") || r.contains("INDUSTRY MARGIN")) {
            if (lenderName != null) {
                String normLender = normalizeLender(lenderName);
                if (normLender.toUpperCase().contains("BAJAJ") && empEnum == EmploymentType.SEP) {
                    return true;
                }
            }
            return empEnum == EmploymentType.SENP || empEnum == EmploymentType.SELF_EMPLOYED;
        }

        return rowEnum == empEnum || r.equalsIgnoreCase(a);
    }
}
