package com.pryme.Backend.eligibility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require",
    "spring.datasource.username=neondb_owner",
    "spring.datasource.password=npg_VbzCd0Anf8oZ",
    "spring.datasource.driver-class-name=org.postgresql.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
@Disabled("Superseded by EligibilityProfileIntegrationTest — per-profile tests with multi-layer assertions")
public class EligibilityProfilesTest {

    @MockBean
    private RedisClient redisClient;

    @MockBean
    private ProxyManager<byte[]> proxyManager;

    @Autowired
    private EligibilityEngineService eligibilityEngineService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAll18Profiles() throws IOException {
        File file = new File("test_profiles.md");
        if (!file.exists()) {
            fail("test_profiles.md not found in the workspace root!");
        }

        String content = Files.readString(file.toPath());
        String[] parts = content.split("## Profile ");

        System.out.println("======================================================================");
        System.out.println("                   ELIGIBILITY ENGINE PROFILE VALIDATION              ");
        System.out.println("======================================================================");

        int passed = 0;
        int failed = 0;
        List<String> failureDetails = new ArrayList<>();

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            
            // Extract Profile ID and Header
            String headerLine = part.split("\n")[0].trim();
            int profileId = -1;
            Pattern idPattern = Pattern.compile("^(\\d+)");
            Matcher idMatcher = idPattern.matcher(headerLine);
            if (idMatcher.find()) {
                profileId = Integer.parseInt(idMatcher.group(1));
            } else {
                continue;
            }

            // Extract JSON payload
            int startJson = part.indexOf("```json");
            if (startJson == -1) {
                System.err.println("Profile " + profileId + ": Missing JSON block");
                continue;
            }
            int endJson = part.indexOf("```", startJson + 7);
            if (endJson == -1) {
                System.err.println("Profile " + profileId + ": Malformed JSON block");
                continue;
            }
            String jsonContent = part.substring(startJson + 7, endJson).trim();

            // Deserialize request
            EligibilityRequest request;
            try {
                request = objectMapper.readValue(jsonContent, EligibilityRequest.class);
            } catch (Exception e) {
                System.err.println("Profile " + profileId + ": JSON mapping failed. Error: " + e.getMessage());
                failed++;
                failureDetails.add("Profile " + profileId + " (" + headerLine + "): JSON Parsing Error - " + e.getMessage());
                continue;
            }

            // Extract Expected Products
            List<String> expectedProducts = new ArrayList<>();
            int startOffersSection = part.indexOf("Expected Products in");
            if (startOffersSection != -1) {
                int endOffersSection = part.indexOf("Expected Excluded", startOffersSection);
                if (endOffersSection == -1) {
                    endOffersSection = part.indexOf("---", startOffersSection);
                }
                if (endOffersSection == -1) {
                    endOffersSection = part.length();
                }
                String offersSectionText = part.substring(startOffersSection, endOffersSection);
                
                Pattern productPattern = Pattern.compile("-\\s+`([A-Z0-9_-]+)`");
                Matcher productMatcher = productPattern.matcher(offersSectionText);
                while (productMatcher.find()) {
                    expectedProducts.add(productMatcher.group(1));
                }
            }

            // Execute eligibility evaluation directly against service
            List<EligibilityResult> results = eligibilityEngineService.evaluate(request);

            // Extract eligible products mapped to public codes
            List<String> actualProducts = results.stream()
                    .filter(EligibilityResult::isEligible)
                    .map(EligibilityResult::toPublicResult)
                    .map(EligibilityResult::productCode)
                    .filter(code -> code != null)
                    .collect(Collectors.toList());

            // Sort lists to compare correctly
            Collections.sort(expectedProducts);
            Collections.sort(actualProducts);

            boolean match = expectedProducts.equals(actualProducts);
            String statusIndicator = match ? "✅ PASS" : "❌ FAIL";

            System.out.printf("Profile %2d | Expected: %-60s | Actual: %-60s | %s\n", 
                    profileId, expectedProducts, actualProducts, statusIndicator);

            if (match) {
                passed++;
            } else {
                failed++;
                StringBuilder details = new StringBuilder();
                details.append(String.format("Profile %d (%s):\n  Expected: %s\n  Actual:   %s\n", 
                        profileId, headerLine, expectedProducts, actualProducts));
                details.append("  All Evaluated Products:\n");
                for (EligibilityResult res : results) {
                    String publicCode = null;
                    if (res.productCode() != null) {
                        String[] pParts = res.productCode().split("-");
                        if (pParts.length >= 2) {
                            publicCode = pParts[0] + "-" + pParts[1];
                        } else {
                            publicCode = res.productCode();
                        }
                    }
                    details.append(String.format("    - %s (%s) | Eligible: %b | Reasons: %s | Notes: %s\n",
                            res.productCode(), publicCode, res.isEligible(), res.rejectionReasons(), res.notes()));
                }
                failureDetails.add(details.toString());
            }
        }

        System.out.println("======================================================================");
        System.out.println(" SUMMARY: Passed " + passed + " / Failed " + failed);
        System.out.println("======================================================================");

        if (failed > 0) {
            for (String detail : failureDetails) {
                System.out.println(detail);
                System.out.println("----------------------------------------------------------------------");
            }
            fail("Some profiles failed eligibility validation check. See logs above.");
        }
    }
}
