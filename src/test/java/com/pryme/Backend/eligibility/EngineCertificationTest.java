package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.audit.certification.CertificationReportModels;
import com.pryme.Backend.eligibility.audit.certification.CertificationService;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/testdb?sslmode=require&channelBinding=require",
        "spring.datasource.username=neondb_owner",
        "spring.datasource.password=npg_VbzCd0Anf8oZ",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
@ActiveProfiles("test")
@DisplayName("Eligibility Engine Certification — workbook-to-engine mathematical validation gate")
public class EngineCertificationTest {

    @MockBean private RedisClient redisClient;
    @MockBean private ProxyManager<byte[]> proxyManager;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private com.pryme.Backend.eligibility.policy.repository.PolicyBundleEntityRepository policyBundleEntityRepository;

    @Autowired
    private com.pryme.Backend.eligibility.policy.repository.PolicyActivationHistoryRepository policyActivationHistoryRepository;

    @Autowired
    private com.pryme.Backend.eligibility.policy.deployment.PolicyActivationService policyActivationService;

    @Test
    @DisplayName("Verify full 10-phase eligibility certification report")
    public void runFullCertificationGate() {
        CertificationReportModels.CertificationReport report = certificationService.runCertification();
        
        assertNotNull(report, "Certification report must be generated");
        assertNotNull(report.certificationId(), "Certification ID must be populated");
        assertNotNull(report.fingerprint(), "Certification fingerprint must be populated");
        
        System.out.println("=== ELIGIBILITY CERTIFICATION RUN ===");
        System.out.println("ID: " + report.certificationId());
        System.out.println("Fingerprint: " + report.fingerprint());
        System.out.println("Workbook Hash: " + report.workbookHash());
        System.out.println("Engine Version: " + report.engineVersion());
        System.out.println("Master Data Version: " + report.masterDataVersion());
        System.out.println("Master Data Matches: " + report.masterDataMatchPercentage() + "%");
        System.out.println("Rules Covered: " + report.ruleCoveragePercentage() + "%");
        System.out.println("Spreadsheet Replays Passed: " + report.replayPassPercentage() + "%");
        System.out.println("Certified: " + report.certified());
        System.out.println("=====================================");

        // Print details of mismatches if any
        if (!report.certified()) {
            System.err.println("Mismatches/Exceptions found:");
            for (var gate : report.gates()) {
                System.err.println(String.format("Gate %s: %s", gate.gate(), gate.pass() ? "PASS" : "FAIL - " + gate.message()));
            }
            for (var mismatch : report.classificationReport().classifiedMismatches()) {
                System.err.println(String.format("[%s] Field %s for key %s mismatched. Expected: %s, Got: %s. Classification: %s. Remediation: %s",
                    mismatch.type(), mismatch.field(), mismatch.key(), mismatch.expected(), mismatch.actual(), mismatch.classification(), mismatch.remediation()
                ));
            }
        }

        // We assert true here to produce a full Exception/Remediation log,
        // which helps engineers resolve individual matrix values, rather than just breaking compiles.
        assertTrue(report.certified(), "Engine certification failed! Check console logs for remediation instructions.");

        // Verify persistent bundle entity creation
        var bundles = policyBundleEntityRepository.findAll();
        assertFalse(bundles.isEmpty(), "PolicyBundleEntity must be persisted in database");
        var latestBundle = bundles.get(bundles.size() - 1);
        assertEquals("CERTIFIED", latestBundle.getState(), "Persisted bundle state must be CERTIFIED");
        assertFalse(latestBundle.isActive(), "Persisted bundle must not be ACTIVE yet");

        // Verify activation workflow
        var activationHistoryBefore = policyActivationHistoryRepository.findAll();
        int initialHistorySize = activationHistoryBefore.size();

        var activationLog = policyActivationService.activate(
                latestBundle.getBundleId(),
                "PrincipalEngineer",
                "ComplianceCommittee",
                "Verification run for deployment gates"
        );

        assertNotNull(activationLog, "Activation log must be created");
        assertNotNull(activationLog.getActivationId(), "Activation ID must be generated");
        assertEquals(latestBundle.getBundleId(), activationLog.getBundleId(), "Activation log must point to the activated bundle ID");

        var updatedBundle = policyBundleEntityRepository.findByBundleId(latestBundle.getBundleId()).orElse(null);
        assertNotNull(updatedBundle, "Activated bundle must exist");
        assertTrue(updatedBundle.isActive(), "Activated bundle must be marked active");
        assertEquals("ACTIVE", updatedBundle.getState(), "Activated bundle state must transition to ACTIVE");

        var activationHistoryAfter = policyActivationHistoryRepository.findAll();
        assertEquals(initialHistorySize + 1, activationHistoryAfter.size(), "One new entry must be added to PolicyActivationHistory");
    }

    @Autowired
    private com.pryme.Backend.loanproduct.repository.LoanProductRepository loanProductRepository;
    @Autowired
    private com.pryme.Backend.eligibility.repository.EligibilityConditionRepository eligibilityConditionRepository;
    @Autowired
    private com.pryme.Backend.loanproduct.repository.ProductRoiMatrixRepository productRoiMatrixRepository;

    @Test
    public void debugDbData() {
        System.out.println("=== DEBUGGING DB DATA ===");
        var products = loanProductRepository.findAll();
        for (var p : products) {
            if (true) {
                System.out.println(String.format("PRODUCT: code=%s, name=%s, lender=%s, type=%s, id=%d",
                        p.getProductCode(), p.getProductName(), p.getLenderName(), p.getLoanType(), p.getId()));
            }
        }

        var conditions = eligibilityConditionRepository.findAll();
        for (var c : conditions) {
            if (true) {
                System.out.println(String.format("CONDITION: productCode=%s, lender=%s, employmentType=%s, surrogate=%s, minAge=%d, maxAge=%d, minIncome=%s",
                        c.getProductCode(), c.getBankName(), c.getEmploymentType(), c.getSurrogate(), c.getMinAge(), c.getMaxAge(), c.getMinIncome()));
            }
        }

        var rois = productRoiMatrixRepository.findAll();
        for (var r : rois) {
            System.out.println(String.format("ROI_ROW: productId=%d, employmentType=%s, minAmt=%s, maxAmt=%s, minCibil=%s, maxCibil=%s, isNtc=%s, roi=%s",
                    r.getProductId(), r.getEmploymentType(), r.getMinLoanAmount(), r.getMaxLoanAmount(), r.getMinCibil(), r.getMaxCibil(), r.isNtc(), r.getRoi()));
        }
    }
}
