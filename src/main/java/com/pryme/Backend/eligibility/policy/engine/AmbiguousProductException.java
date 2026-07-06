package com.pryme.Backend.eligibility.policy.engine;

/**
 * ❌ Exception thrown when multiple candidate products match.
 */
public class AmbiguousProductException extends RuntimeException {
    public AmbiguousProductException(String message) {
        super(message);
    }
}
