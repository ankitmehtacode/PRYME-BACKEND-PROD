package com.pryme.Backend.eligibility.policy.engine;

/**
 * ❌ Exception thrown when no matching product is found.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}
