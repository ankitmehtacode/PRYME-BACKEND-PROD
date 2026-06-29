package com.pryme.Backend.eligibility;

import java.util.Set;

/**
 * Encapsulates expected results for a single eligibility profile test.
 *
 * <p>This is a value object used by {@link EligibilityProfileIntegrationTest}
 * to declaratively assert engine output without scattering magic strings
 * across test methods.</p>
 *
 * @param profileId            Human-readable ID (1–18)
 * @param description          One-line scenario description for diagnostic output
 * @param expectEligible       Whether the profile should produce at least one eligible offer
 * @param expectedProductCodes Exact set of eligible public product codes (e.g. "BOB-HL")
 * @param excludedProductCodes Product codes that MUST NOT appear in the eligible set
 * @param expectedOfferCount   Number of expected eligible offers (redundant with set size — used for fast-fail)
 * @param rejectionKeyword     If non-null, at least one rejection reason must contain this substring (for rejection profiles)
 */
public record ProfileExpectation(
        int profileId,
        String description,
        boolean expectEligible,
        Set<String> expectedProductCodes,
        Set<String> excludedProductCodes,
        int expectedOfferCount,
        String rejectionKeyword
) {

    /** Convenience factory for eligible profiles. */
    public static ProfileExpectation eligible(int id, String desc, Set<String> expected, Set<String> excluded) {
        return new ProfileExpectation(id, desc, true, expected, excluded, expected.size(), null);
    }

    /** Convenience factory for rejected profiles (geo-fence, no products, etc.). */
    public static ProfileExpectation rejected(int id, String desc, String keyword) {
        return new ProfileExpectation(id, desc, false, Set.of(), Set.of(), 0, keyword);
    }
}
