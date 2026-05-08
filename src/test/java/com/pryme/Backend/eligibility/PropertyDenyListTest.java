package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the smartEvaluatePropertyDenyList method — ensures negativeProperty
 * deny-lists correctly block sub-types AND their resolved categories.
 */
@DisplayName("Property Deny-List — smartEvaluatePropertyDenyList()")
class PropertyDenyListTest {

    private final EligibilityEngineService engine;

    PropertyDenyListTest() throws Exception {
        // Create engine with null deps — we only test the private static-like method
        var ctor = EligibilityEngineService.class.getDeclaredConstructors()[0];
        ctor.setAccessible(true);
        Object[] nullArgs = new Object[ctor.getParameterCount()];
        engine = (EligibilityEngineService) ctor.newInstance(nullArgs);
    }

    private boolean evaluate(String denyList, String rawSubType, String resolvedCat) throws Exception {
        Method m = EligibilityEngineService.class.getDeclaredMethod(
                "smartEvaluatePropertyDenyList", String.class, String.class, String.class);
        m.setAccessible(true);
        return (boolean) m.invoke(engine, denyList, rawSubType, resolvedCat);
    }

    // ── Basic deny-list scenarios ────────────────────────────────────────────
    @Test void null_deny_list_passes() throws Exception {
        assertTrue(evaluate(null, "FLAT", "RESIDENTIAL"));
    }
    @Test void empty_deny_list_passes() throws Exception {
        assertTrue(evaluate("  ", "FLAT", "RESIDENTIAL"));
    }

    // ── Single-word deny (from DB: negative_property = 'Plot') ──────────────
    @Test void plot_denied_by_single_word() throws Exception {
        assertFalse(evaluate("Plot", "PLOT", "PLOT"));
    }
    @Test void flat_not_denied_by_plot() throws Exception {
        assertTrue(evaluate("Plot", "FLAT", "RESIDENTIAL"));
    }
    @Test void home_not_denied_by_plot() throws Exception {
        assertTrue(evaluate("Plot", "HOME", "RESIDENTIAL"));
    }

    // ── Multi-value deny-list ───────────────────────────────────────────────
    @Test void semicolon_separated_deny() throws Exception {
        assertFalse(evaluate("Plot;Land;Farmhouse", "LAND", "LAND"));
    }
    @Test void comma_separated_deny() throws Exception {
        assertFalse(evaluate("Plot,Land,Farmhouse", "PLOT", "PLOT"));
    }
    @Test void passes_when_not_in_list() throws Exception {
        assertTrue(evaluate("Plot;Land;Farmhouse", "FLAT", "RESIDENTIAL"));
    }

    // ── Category-level matching ─────────────────────────────────────────────
    @Test void deny_by_category_blocks_all_subtypes() throws Exception {
        // If deny-list says "RESIDENTIAL", even FLAT should be blocked
        assertFalse(evaluate("RESIDENTIAL", "FLAT", "RESIDENTIAL"));
    }

    // ── Case insensitivity ──────────────────────────────────────────────────
    @Test void case_insensitive_match() throws Exception {
        assertFalse(evaluate("plot", "PLOT", "PLOT"));
    }
    @Test void mixed_case_match() throws Exception {
        assertFalse(evaluate("Plot", "plot", "plot"));
    }
}
