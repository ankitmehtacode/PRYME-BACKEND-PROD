package com.pryme.Backend.eligibility.service;

import com.pryme.Backend.eligibility.policy.engine.ResolverRegistry;
import com.pryme.Backend.eligibility.policy.provider.ActiveBundlePolicyProvider;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LowLtvSurrogateService {

    private final ResolverRegistry resolverRegistry;
    private final ActiveBundlePolicyProvider activeBundlePolicyProvider;

    public BigDecimal getLapLtv(String lenderName, String propertyKey) {
        var active = activeBundlePolicyProvider.getActiveBundle();
        if (active == null) return null;
        return resolverRegistry.getLowLtvResolver().resolve(active, "LAP", lenderName, propertyKey, BigDecimal.ZERO);
    }

    public BigDecimal getHlLtv(String propertyType, BigDecimal loanAmount) {
        var active = activeBundlePolicyProvider.getActiveBundle();
        if (active == null) return null;
        return resolverRegistry.getLowLtvResolver().resolve(active, "HL", "N/A", propertyType, loanAmount);
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
