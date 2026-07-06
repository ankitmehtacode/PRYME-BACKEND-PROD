package com.pryme.Backend.eligibility.service;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * Registry-based alias mapping for lender names used across certification workbooks.
 * <p>
 * This decouples alias resolution from the normalizer, enabling future CRM-based management.
 * Each alias group maps to a single canonical name used for cross-sheet matching.
 */
@Component
public class AliasRegistry {

    private final Map<String, String> canonicalMap;

    public AliasRegistry() {
        Map<String, String> map = new LinkedHashMap<>();
        // Bajaj Finance ↔ Bajaj Prime → Canonical: Bajaj Prime
        // FOIR, PF, and Login sheets use "Bajaj Prime"
        map.put("BAJAJ FINANCE", "Bajaj Prime");
        map.put("BAJAJ PRIME", "Bajaj Prime");
        this.canonicalMap = Collections.unmodifiableMap(map);
    }

    /**
     * Resolves a lender name to its canonical form for cross-sheet matching.
     * If no alias is registered, returns the input unchanged.
     */
    public String resolveCanonical(String lenderName) {
        if (lenderName == null) return "";
        String key = lenderName.trim().toUpperCase();
        return canonicalMap.getOrDefault(key, lenderName.trim());
    }

    /**
     * Returns true if two lender names are aliases of each other.
     */
    public boolean areAliases(String lender1, String lender2) {
        if (lender1 == null || lender2 == null) return false;
        return resolveCanonical(lender1).equalsIgnoreCase(resolveCanonical(lender2));
    }
}
