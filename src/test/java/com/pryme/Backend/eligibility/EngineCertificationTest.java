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
        "spring.datasource.url=jdbc:postgresql://ep-empty-boat-a1abgqec-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channelBinding=require",
        "spring.datasource.username=neondb_owner",
        "spring.datasource.password=npg_VbzCd0Anf8oZ",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=false"
})
@ActiveProfiles("test")
@DisplayName("Eligibility Engine Certification — workbook-to-engine mathematical validation gate")
public class EngineCertificationTest {

    @MockBean private RedisClient redisClient;
    @MockBean private ProxyManager<byte[]> proxyManager;

    @Autowired
    private CertificationService certificationService;

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
    }
}
