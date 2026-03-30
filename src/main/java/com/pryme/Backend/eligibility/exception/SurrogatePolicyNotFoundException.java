// File: src/main/java/com/pryme/Backend/eligibility/exception/SurrogatePolicyNotFoundException.java

package com.pryme.Backend.eligibility.exception;

/**
 * Thrown by SurrogateIncomeResolver when no policy exists for the
 * requested lender + loan type + program combination.
 * Caught in EligibilityEngineService per-product loop — produces
 * an ineligible result rather than crashing the entire evaluation.
 */
public class SurrogatePolicyNotFoundException extends RuntimeException {

    public SurrogatePolicyNotFoundException(String message) {
        super(message);
    }
}
