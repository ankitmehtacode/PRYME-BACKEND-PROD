package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.service.LowLtvSurrogateService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LowLtvSurrogateServiceTest {

    private final LowLtvSurrogateService service = new LowLtvSurrogateService();

    @Test
    void testNormalizeLender() {
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("L&T Finance", "RES_FLAT"));
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("l and t finance", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("ICICI Bank", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("icici", "RES_FLAT"));
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("Bandhan Bank", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("Aditya Birla Finance Limited", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("ABFL", "RES_FLAT"));
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("Bank of Baroda", "RES_FLAT"));
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("Bajaj Finance", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("Yes Bank", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("HDFC Bank", "RES_FLAT"));
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("IDFC FIRST Bank", "RES_FLAT"));
        assertEquals(new BigDecimal("0.75"), service.getLapLtv("JIO Finance", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("IDBI Bank", "RES_FLAT"));
        assertEquals(new BigDecimal("0.70"), service.getLapLtv("TATA Capital", "RES_FLAT"));
    }

    @Test
    void testGetLapLtvNegativeAndMissing() {
        // Bandhan Residential Plot is Negative
        assertNull(service.getLapLtv("Bandhan Bank", "RES_PLOT"));
        
        // Hospital for L&T is Negative
        assertNull(service.getLapLtv("L&T Finance", "COM_HOSPITAL"));

        // Unknown lender should return null
        assertNull(service.getLapLtv("NonExistentBank", "RES_FLAT"));
    }

    @Test
    void testGetHlLtv() {
        // Plot: flat 70%
        assertEquals(new BigDecimal("0.70"), service.getHlLtv("PLOT", new BigDecimal("5000000")));
        assertEquals(new BigDecimal("0.70"), service.getHlLtv("LAND", new BigDecimal("10000000")));

        // Ready Built (Flat/Apartment/House/Residential/etc.):
        // 0 to 30L: 90%
        assertEquals(new BigDecimal("0.90"), service.getHlLtv("FLAT", new BigDecimal("2500000")));
        assertEquals(new BigDecimal("0.90"), service.getHlLtv("RESIDENTIAL", new BigDecimal("3000000")));

        // 30L+1 to 75L: 80%
        assertEquals(new BigDecimal("0.80"), service.getHlLtv("FLAT", new BigDecimal("3000001")));
        assertEquals(new BigDecimal("0.80"), service.getHlLtv("FLAT", new BigDecimal("5000000")));
        assertEquals(new BigDecimal("0.80"), service.getHlLtv("FLAT", new BigDecimal("7500000")));

        // 75L+1+: 75%
        assertEquals(new BigDecimal("0.75"), service.getHlLtv("FLAT", new BigDecimal("7500001")));
        assertEquals(new BigDecimal("0.75"), service.getHlLtv("FLAT", new BigDecimal("10000000")));
    }

    @Test
    void testResolvePropertyKey() {
        // Residential Flat mappings
        assertEquals("RES_FLAT", service.resolvePropertyKey("FLAT", null, null));
        assertEquals("RES_FLAT", service.resolvePropertyKey("HOME", null, null));
        
        // Plot mappings depending on categories
        assertEquals("RES_PLOT", service.resolvePropertyKey("PLOT", "RESIDENTIAL", null));
        assertEquals("COM_PLOT", service.resolvePropertyKey("PLOT", "COMMERCIAL", null));
        assertEquals("IND_PLOT", service.resolvePropertyKey("PLOT", null, "INDUSTRIAL"));
        assertEquals("RES_PLOT", service.resolvePropertyKey("PLOT", null, null)); // default to Residential Plot

        // Subtypes
        assertEquals("COM_HOSPITAL", service.resolvePropertyKey("HOSPITAL", null, null));
        assertEquals("IND_FACTORIES", service.resolvePropertyKey("FACTORIES", null, null));
    }
}
