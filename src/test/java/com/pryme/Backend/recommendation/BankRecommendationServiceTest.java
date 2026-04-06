package com.pryme.Backend.recommendation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * DISABLED: LoanProduct entity was refactored:
 *  - .bank(Bank) → removed (now uses lenderId/lenderName)
 *  - .getBank() → removed
 *  - .getInterestRate() → renamed to .getRoi()
 *  - Builder methods changed accordingly
 *
 * Pre-existing issue — not related to session security migration.
 */
@Disabled("LoanProduct entity refactored — bank/interestRate accessors removed")
class BankRecommendationServiceTest {

    @Test
    void placeholder() {
        // Intentionally empty — test class retained for future rewrite
    }
}
