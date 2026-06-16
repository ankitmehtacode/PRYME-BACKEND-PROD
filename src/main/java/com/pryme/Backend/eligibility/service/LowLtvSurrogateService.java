// File: src/main/java/com/pryme/Backend/eligibility/service/LowLtvSurrogateService.java

package com.pryme.Backend.eligibility.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class LowLtvSurrogateService {

    // LAP matrix: lender key -> (property type -> LTV value)
    private static final Map<String, Map<String, BigDecimal>> LAP_MATRIX = new HashMap<>();

    static {
        // Initialize LAP matrix for L&T Finance
        Map<String, BigDecimal> lt = new HashMap<>();
        lt.put("RES_PLOT", new BigDecimal("0.75"));
        lt.put("RES_FLAT", new BigDecimal("0.75"));
        lt.put("COM_HOSTEL", new BigDecimal("0.65"));
        lt.put("COM_RESTAURANT", new BigDecimal("0.65"));
        lt.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.65"));
        lt.put("COM_WAREHOUSE", new BigDecimal("0.65"));
        lt.put("COM_SHOP", new BigDecimal("0.65"));
        lt.put("COM_GODOWN", new BigDecimal("0.65"));
        lt.put("IND_FACTORIES", new BigDecimal("0.50"));
        lt.put("IND_PLOT", new BigDecimal("0.50"));
        lt.put("IND_WAREHOUSES", new BigDecimal("0.50"));
        lt.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.50"));
        lt.put("IND_RD_FACILITY", new BigDecimal("0.50"));
        lt.put("IND_FLEX_SPACES", new BigDecimal("0.50"));
        LAP_MATRIX.put("lt", lt);

        // ICICI Bank
        Map<String, BigDecimal> icici = new HashMap<>();
        icici.put("RES_PLOT", new BigDecimal("0.45"));
        icici.put("RES_FLAT", new BigDecimal("0.70"));
        icici.put("COM_PLOT", new BigDecimal("0.45"));
        icici.put("COM_HOSTEL", new BigDecimal("0.60"));
        icici.put("COM_RESTAURANT", new BigDecimal("0.60"));
        icici.put("COM_HOTEL", new BigDecimal("0.60"));
        icici.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.60"));
        icici.put("COM_WAREHOUSE", new BigDecimal("0.60"));
        icici.put("COM_SHOP", new BigDecimal("0.60"));
        icici.put("COM_GODOWN", new BigDecimal("0.60"));
        icici.put("IND_FACTORIES", new BigDecimal("0.60"));
        icici.put("IND_PLOT", new BigDecimal("0.60"));
        icici.put("IND_WAREHOUSES", new BigDecimal("0.60"));
        icici.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.60"));
        icici.put("IND_RD_FACILITY", new BigDecimal("0.60"));
        icici.put("IND_FLEX_SPACES", new BigDecimal("0.60"));
        LAP_MATRIX.put("icici", icici);

        // Bandhan Bank
        Map<String, BigDecimal> bandhan = new HashMap<>();
        bandhan.put("RES_FLAT", new BigDecimal("0.75"));
        bandhan.put("COM_HOSTEL", new BigDecimal("0.75"));
        bandhan.put("COM_RESTAURANT", new BigDecimal("0.75"));
        bandhan.put("COM_HOTEL", new BigDecimal("0.75"));
        bandhan.put("COM_WAREHOUSE", new BigDecimal("0.75"));
        bandhan.put("COM_SCHOOL", new BigDecimal("0.75"));
        bandhan.put("COM_SHOP", new BigDecimal("0.75"));
        bandhan.put("COM_GODOWN", new BigDecimal("0.75"));
        LAP_MATRIX.put("bandhan", bandhan);

        // Aditya Birla
        Map<String, BigDecimal> abfl = new HashMap<>();
        abfl.put("RES_PLOT", new BigDecimal("0.50"));
        abfl.put("RES_FLAT", new BigDecimal("0.70"));
        abfl.put("COM_PLOT", new BigDecimal("0.50"));
        abfl.put("COM_HOSTEL", new BigDecimal("0.60"));
        abfl.put("COM_RESTAURANT", new BigDecimal("0.60"));
        abfl.put("COM_HOTEL", new BigDecimal("0.60"));
        abfl.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.50"));
        abfl.put("COM_WAREHOUSE", new BigDecimal("0.60"));
        abfl.put("COM_SHOP", new BigDecimal("0.60"));
        abfl.put("COM_GODOWN", new BigDecimal("0.60"));
        abfl.put("IND_FACTORIES", new BigDecimal("0.50"));
        abfl.put("IND_PLOT", new BigDecimal("0.50"));
        abfl.put("IND_WAREHOUSES", new BigDecimal("0.50"));
        abfl.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.50"));
        abfl.put("IND_RD_FACILITY", new BigDecimal("0.50"));
        abfl.put("IND_FLEX_SPACES", new BigDecimal("0.50"));
        LAP_MATRIX.put("abfl", abfl);

        // BOB
        Map<String, BigDecimal> bob = new HashMap<>();
        bob.put("RES_PLOT", new BigDecimal("0.75"));
        bob.put("RES_FLAT", new BigDecimal("0.75"));
        bob.put("COM_PLOT", new BigDecimal("0.65"));
        bob.put("COM_HOSTEL", new BigDecimal("0.65"));
        bob.put("COM_RESTAURANT", new BigDecimal("0.65"));
        bob.put("COM_HOTEL", new BigDecimal("0.65"));
        bob.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.65"));
        bob.put("COM_WAREHOUSE", new BigDecimal("0.65"));
        bob.put("COM_SHOP", new BigDecimal("0.65"));
        bob.put("COM_GODOWN", new BigDecimal("0.65"));
        bob.put("IND_FACTORIES", new BigDecimal("0.50"));
        bob.put("IND_PLOT", new BigDecimal("0.50"));
        bob.put("IND_WAREHOUSES", new BigDecimal("0.50"));
        bob.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.50"));
        bob.put("IND_RD_FACILITY", new BigDecimal("0.50"));
        bob.put("IND_FLEX_SPACES", new BigDecimal("0.50"));
        LAP_MATRIX.put("bob", bob);

        // SBI
        Map<String, BigDecimal> sbi = new HashMap<>();
        sbi.put("RES_PLOT", new BigDecimal("0.65"));
        sbi.put("RES_FLAT", new BigDecimal("0.65"));
        sbi.put("COM_HOSPITAL", new BigDecimal("0.65"));
        sbi.put("COM_PLOT", new BigDecimal("0.65"));
        sbi.put("COM_HOSTEL", new BigDecimal("0.65"));
        sbi.put("COM_RESTAURANT", new BigDecimal("0.65"));
        sbi.put("COM_HOTEL", new BigDecimal("0.65"));
        sbi.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.65"));
        sbi.put("COM_WAREHOUSE", new BigDecimal("0.65"));
        sbi.put("COM_SCHOOL", new BigDecimal("0.65"));
        sbi.put("COM_SHOP", new BigDecimal("0.65"));
        sbi.put("COM_GODOWN", new BigDecimal("0.65"));
        sbi.put("IND_FACTORIES", new BigDecimal("0.65"));
        sbi.put("IND_PLOT", new BigDecimal("0.65"));
        sbi.put("IND_WAREHOUSES", new BigDecimal("0.65"));
        sbi.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.65"));
        sbi.put("IND_RD_FACILITY", new BigDecimal("0.65"));
        sbi.put("IND_FLEX_SPACES", new BigDecimal("0.65"));
        LAP_MATRIX.put("sbi", sbi);

        // Bajaj
        Map<String, BigDecimal> bajaj = new HashMap<>();
        bajaj.put("RES_PLOT", new BigDecimal("0.75"));
        bajaj.put("RES_FLAT", new BigDecimal("0.75"));
        bajaj.put("COM_PLOT", new BigDecimal("0.65"));
        bajaj.put("COM_SHOP", new BigDecimal("0.65"));
        bajaj.put("COM_GODOWN", new BigDecimal("0.65"));
        LAP_MATRIX.put("bajaj", bajaj);

        // Yes Bank
        Map<String, BigDecimal> yes = new HashMap<>();
        yes.put("RES_FLAT", new BigDecimal("0.70"));
        yes.put("COM_HOSPITAL", new BigDecimal("0.60"));
        yes.put("COM_HOSTEL", new BigDecimal("0.60"));
        yes.put("COM_RESTAURANT", new BigDecimal("0.60"));
        yes.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.60"));
        yes.put("COM_WAREHOUSE", new BigDecimal("0.60"));
        yes.put("COM_SHOP", new BigDecimal("0.60"));
        yes.put("COM_GODOWN", new BigDecimal("0.60"));
        LAP_MATRIX.put("yes", yes);

        // HDFC Bank
        Map<String, BigDecimal> hdfc = new HashMap<>();
        hdfc.put("RES_PLOT", new BigDecimal("0.45"));
        hdfc.put("RES_FLAT", new BigDecimal("0.70"));
        hdfc.put("COM_PLOT", new BigDecimal("0.45"));
        hdfc.put("COM_HOSTEL", new BigDecimal("0.60"));
        hdfc.put("COM_RESTAURANT", new BigDecimal("0.60"));
        hdfc.put("COM_HOTEL", new BigDecimal("0.60"));
        hdfc.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.60"));
        hdfc.put("COM_WAREHOUSE", new BigDecimal("0.60"));
        hdfc.put("COM_SHOP", new BigDecimal("0.60"));
        hdfc.put("COM_GODOWN", new BigDecimal("0.60"));
        hdfc.put("IND_FACTORIES", new BigDecimal("0.60"));
        hdfc.put("IND_PLOT", new BigDecimal("0.60"));
        hdfc.put("IND_WAREHOUSES", new BigDecimal("0.60"));
        hdfc.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.60"));
        hdfc.put("IND_RD_FACILITY", new BigDecimal("0.60"));
        hdfc.put("IND_FLEX_SPACES", new BigDecimal("0.60"));
        LAP_MATRIX.put("hdfc", hdfc);

        // IDFC
        Map<String, BigDecimal> idfc = new HashMap<>();
        idfc.put("RES_PLOT", new BigDecimal("0.60"));
        idfc.put("RES_FLAT", new BigDecimal("0.75"));
        idfc.put("COM_HOSPITAL", new BigDecimal("0.60"));
        idfc.put("COM_PLOT", new BigDecimal("0.60"));
        idfc.put("COM_HOSTEL", new BigDecimal("0.75"));
        idfc.put("COM_RESTAURANT", new BigDecimal("0.75"));
        idfc.put("COM_HOTEL", new BigDecimal("0.60"));
        idfc.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.60"));
        idfc.put("COM_WAREHOUSE", new BigDecimal("0.75"));
        idfc.put("COM_SCHOOL", new BigDecimal("0.60"));
        idfc.put("COM_SHOP", new BigDecimal("0.75"));
        idfc.put("COM_GODOWN", new BigDecimal("0.75"));
        idfc.put("IND_FACTORIES", new BigDecimal("0.75"));
        idfc.put("IND_PLOT", new BigDecimal("0.75"));
        idfc.put("IND_WAREHOUSES", new BigDecimal("0.75"));
        idfc.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.75"));
        idfc.put("IND_RD_FACILITY", new BigDecimal("0.75"));
        idfc.put("IND_FLEX_SPACES", new BigDecimal("0.75"));
        LAP_MATRIX.put("idfc", idfc);

        // Jio Finance
        Map<String, BigDecimal> jio = new HashMap<>();
        jio.put("RES_PLOT", new BigDecimal("0.75"));
        jio.put("RES_FLAT", new BigDecimal("0.75"));
        jio.put("COM_HOSTEL", new BigDecimal("0.65"));
        jio.put("COM_RESTAURANT", new BigDecimal("0.65"));
        jio.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.65"));
        jio.put("COM_WAREHOUSE", new BigDecimal("0.65"));
        jio.put("COM_SHOP", new BigDecimal("0.65"));
        jio.put("COM_GODOWN", new BigDecimal("0.65"));
        jio.put("IND_FACTORIES", new BigDecimal("0.50"));
        jio.put("IND_PLOT", new BigDecimal("0.50"));
        jio.put("IND_WAREHOUSES", new BigDecimal("0.50"));
        jio.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.50"));
        jio.put("IND_RD_FACILITY", new BigDecimal("0.50"));
        jio.put("IND_FLEX_SPACES", new BigDecimal("0.50"));
        LAP_MATRIX.put("jio", jio);

        // IDBI
        Map<String, BigDecimal> idbi = new HashMap<>();
        idbi.put("RES_FLAT", new BigDecimal("0.70"));
        idbi.put("COM_HOSPITAL", new BigDecimal("0.60"));
        idbi.put("COM_HOSTEL", new BigDecimal("0.60"));
        idbi.put("COM_RESTAURANT", new BigDecimal("0.60"));
        idbi.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.60"));
        idbi.put("COM_WAREHOUSE", new BigDecimal("0.60"));
        idbi.put("COM_SHOP", new BigDecimal("0.60"));
        idbi.put("COM_GODOWN", new BigDecimal("0.60"));
        LAP_MATRIX.put("idbi", idbi);

        // TATA Capital
        Map<String, BigDecimal> tata = new HashMap<>();
        tata.put("RES_PLOT", new BigDecimal("0.50"));
        tata.put("RES_FLAT", new BigDecimal("0.70"));
        tata.put("COM_PLOT", new BigDecimal("0.50"));
        tata.put("COM_HOSTEL", new BigDecimal("0.60"));
        tata.put("COM_RESTAURANT", new BigDecimal("0.60"));
        tata.put("COM_HOTEL", new BigDecimal("0.60"));
        tata.put("COM_MARRIAGE_GARDEN", new BigDecimal("0.50"));
        tata.put("COM_WAREHOUSE", new BigDecimal("0.60"));
        tata.put("COM_SHOP", new BigDecimal("0.60"));
        tata.put("COM_GODOWN", new BigDecimal("0.60"));
        tata.put("IND_FACTORIES", new BigDecimal("0.50"));
        tata.put("IND_PLOT", new BigDecimal("0.50"));
        tata.put("IND_WAREHOUSES", new BigDecimal("0.50"));
        tata.put("IND_DISTRIBUTION_CENTERS", new BigDecimal("0.50"));
        tata.put("IND_RD_FACILITY", new BigDecimal("0.50"));
        tata.put("IND_FLEX_SPACES", new BigDecimal("0.50"));
        LAP_MATRIX.put("tata", tata);
    }

    public BigDecimal getLapLtv(String lenderName, String propertyKey) {
        String normLender = normalizeLender(lenderName);
        Map<String, BigDecimal> ltvMap = LAP_MATRIX.get(normLender);
        if (ltvMap == null) return null;
        return ltvMap.get(propertyKey);
    }

    private String normalizeLender(String bankName) {
        if (bankName == null) return "";
        String lower = bankName.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (lower.contains("lt") || lower.contains("landt")) return "lt";
        if (lower.contains("icici")) return "icici";
        if (lower.contains("bandhan")) return "bandhan";
        if (lower.contains("aditya") || lower.contains("abfl")) return "abfl";
        if (lower.contains("baroda") || lower.contains("bob")) return "bob";
        if (lower.contains("sbi") || lower.contains("statebank")) return "sbi";
        if (lower.contains("bajaj")) return "bajaj";
        if (lower.contains("yes")) return "yes";
        if (lower.contains("hdfc")) return "hdfc";
        if (lower.contains("idfc")) return "idfc";
        if (lower.contains("jio")) return "jio";
        if (lower.contains("idbi")) return "idbi";
        if (lower.contains("tata")) return "tata";
        return lower;
    }

    public BigDecimal getHlLtv(String propertyType, BigDecimal loanAmount) {
        if (propertyType == null) return null;
        String pType = propertyType.toUpperCase();
        if (pType.equals("PLOT") || pType.equals("LAND")) {
            return new BigDecimal("0.70"); // Plot flat 70%
        } else {
            // Flat / Apartment / House / Residential
            // 0 - 30L: 90%
            // 30L+1 - 75L: 80%
            // 75L+1+: 75%
            BigDecimal amount = loanAmount != null ? loanAmount : BigDecimal.ZERO;
            if (amount.compareTo(new BigDecimal("3000000")) <= 0) {
                return new BigDecimal("0.90");
            } else if (amount.compareTo(new BigDecimal("7500000")) <= 0) {
                return new BigDecimal("0.80");
            } else {
                return new BigDecimal("0.75");
            }
        }
    }

    public String resolvePropertyKey(String propertyType, String propertyCategory, String businessPropertyCategory) {
        if (propertyType == null) return "RES_FLAT";
        String pType = propertyType.toUpperCase();

        if (pType.equals("FLAT") || pType.equals("HOME") || pType.equals("VILLA") || pType.equals("APARTMENT") || pType.equals("RESIDENTIAL") || pType.equals("ROW_HOUSE") || pType.equals("PENTHOUSE")) {
            return "RES_FLAT";
        }
        if (pType.equals("PLOT") || pType.equals("LAND")) {
            String cat = propertyCategory != null ? propertyCategory.toUpperCase() : "";
            String bCat = businessPropertyCategory != null ? businessPropertyCategory.toUpperCase() : "";

            if (cat.contains("COMMERCIAL") || bCat.contains("COMMERCIAL")) {
                return "COM_PLOT";
            }
            if (cat.contains("INDUSTRIAL") || bCat.contains("INDUSTRIAL")) {
                return "IND_PLOT";
            }
            return "RES_PLOT"; // default to Residential Plot
        }

        // Commercial subtypes
        if (pType.equals("HOSPITAL")) return "COM_HOSPITAL";
        if (pType.equals("HOSTEL")) return "COM_HOSTEL";
        if (pType.equals("RESTAURANTS") || pType.equals("RESTAURANT")) return "COM_RESTAURANT";
        if (pType.equals("HOTEL")) return "COM_HOTEL";
        if (pType.equals("MARRIAGE_GARDEN")) return "COM_MARRIAGE_GARDEN";
        if (pType.equals("WAREHOUSE")) return "COM_WAREHOUSE";
        if (pType.equals("SCHOOL")) return "COM_SCHOOL";
        if (pType.equals("SHOP")) return "COM_SHOP";
        if (pType.equals("GODOWN")) return "COM_GODOWN";

        // Industrial subtypes
        if (pType.equals("FACTORIES") || pType.equals("FACTORY")) return "IND_FACTORIES";
        if (pType.equals("WAREHOUSES")) return "IND_WAREHOUSES";
        if (pType.equals("DISTRIBUTION_CENTER") || pType.equals("DISTRIBUTION_CENTERS")) return "IND_DISTRIBUTION_CENTERS";
        if (pType.equals("R_AND_D_FACILITY")) return "IND_RD_FACILITY";
        if (pType.equals("FLEX_SPACES")) return "IND_FLEX_SPACES";

        return "RES_FLAT"; // fallback
    }
}
