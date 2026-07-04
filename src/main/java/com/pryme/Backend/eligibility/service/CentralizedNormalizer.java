package com.pryme.Backend.eligibility.service;

import org.springframework.stereotype.Component;

@Component
public class CentralizedNormalizer {

    public String normalizeLender(String name) {
        if (name == null) return "";
        String clean = name.trim().toLowerCase();
        if (clean.contains("l&t") || clean.contains("lt ") || clean.contains("ltfinance") || clean.contains("l & t")) {
            return "L&T Finance";
        }
        if (clean.contains("icici")) return "ICICI Bank";
        if (clean.contains("bandhan")) return "Bandhan Bank";
        if (clean.contains("aditya") || clean.contains("abfl") || clean.contains("birla")) {
            return "Aditya Birla Finance Limited";
        }
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
        return name.trim();
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
        if (clean.contains("lap") || clean.contains("against property") || clean.contains("secured loan")) {
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
}
