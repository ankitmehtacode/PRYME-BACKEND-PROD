package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the property-type normalization layer added to EligibilityEngineService.
 * Validates that every frontend sub-type maps to the correct bank-policy category.
 */
@DisplayName("Property Type Normalization — resolvePropertyCategory()")
class EligibilityEnginePropertyTypeTest {

    // Access the private static method via reflection for isolated unit testing
    private String resolve(String propertyType) throws Exception {
        Method m = EligibilityEngineService.class.getDeclaredMethod("resolvePropertyCategory", String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, propertyType);
    }

    // ── RESIDENTIAL sub-types ───────────────────────────────────────────────
    @Test void flat_maps_to_residential() throws Exception { assertEquals("RESIDENTIAL", resolve("FLAT")); }
    @Test void home_maps_to_residential() throws Exception { assertEquals("RESIDENTIAL", resolve("HOME")); }
    @Test void villa_maps_to_residential() throws Exception { assertEquals("RESIDENTIAL", resolve("VILLA")); }
    @Test void apartment_maps_to_residential() throws Exception { assertEquals("RESIDENTIAL", resolve("APARTMENT")); }
    @Test void residential_identity() throws Exception { assertEquals("RESIDENTIAL", resolve("RESIDENTIAL")); }
    @Test void case_insensitive_flat() throws Exception { assertEquals("RESIDENTIAL", resolve("flat")); }
    @Test void case_insensitive_home() throws Exception { assertEquals("RESIDENTIAL", resolve("Home")); }

    // ── COMMERCIAL sub-types ────────────────────────────────────────────────
    @Test void hospital_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("HOSPITAL")); }
    @Test void hostel_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("HOSTEL")); }
    @Test void restaurants_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("RESTAURANTS")); }
    @Test void hotel_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("HOTEL")); }
    @Test void marriage_garden_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("MARRIAGE_GARDEN")); }
    @Test void school_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("SCHOOL")); }
    @Test void shop_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("SHOP")); }
    @Test void warehouse_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("WAREHOUSE")); }
    @Test void godown_maps_to_commercial() throws Exception { assertEquals("COMMERCIAL", resolve("GODOWN")); }
    @Test void commercial_identity() throws Exception { assertEquals("COMMERCIAL", resolve("COMMERCIAL")); }

    // ── INDUSTRIAL sub-types ────────────────────────────────────────────────
    @Test void factories_maps_to_industrial() throws Exception { assertEquals("INDUSTRIAL", resolve("FACTORIES")); }
    @Test void warehouses_maps_to_industrial() throws Exception { assertEquals("INDUSTRIAL", resolve("WAREHOUSES")); }
    @Test void distribution_center_maps_to_industrial() throws Exception { assertEquals("INDUSTRIAL", resolve("DISTRIBUTION_CENTER")); }
    @Test void rnd_facility_maps_to_industrial() throws Exception { assertEquals("INDUSTRIAL", resolve("R_AND_D_FACILITY")); }
    @Test void flex_spaces_maps_to_industrial() throws Exception { assertEquals("INDUSTRIAL", resolve("FLEX_SPACES")); }
    @Test void industrial_identity() throws Exception { assertEquals("INDUSTRIAL", resolve("INDUSTRIAL")); }

    // ── PLOT / LAND ─────────────────────────────────────────────────────────
    @Test void plot_maps_to_plot() throws Exception { assertEquals("PLOT", resolve("PLOT")); }
    @Test void land_maps_to_land() throws Exception { assertEquals("LAND", resolve("LAND")); }

    // ── Edge cases ──────────────────────────────────────────────────────────
    @Test void null_defaults_to_residential() throws Exception { assertEquals("RESIDENTIAL", resolve(null)); }
    @Test void unknown_type_passthrough() throws Exception { assertEquals("BUNGALOW", resolve("BUNGALOW")); }
}
