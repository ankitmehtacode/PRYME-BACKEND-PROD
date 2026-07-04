package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.audit.*;
import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
@DisplayName("Eligibility Engine — Golden Dataset Audit Trace Test Suite")
public class EligibilityAuditTest {

    @MockBean private RedisClient redisClient;
    @MockBean private ProxyManager<byte[]> proxyManager;

    @Autowired private EligibilityEngineService engine;

    @Test
    @DisplayName("Run 300+ Deterministic Scenarios and Validate Immutable Traces")
    public void test300PlusGoldenScenarios() {
        List<EligibilityRequest> scenarios = generateGoldenScenarios();
        assertTrue(scenarios.size() >= 300, "Should generate at least 300 scenarios, generated: " + scenarios.size());

        int count = 0;
        for (EligibilityRequest request : scenarios) {
            List<EligibilityResult> results = engine.evaluate(request);
            assertNotNull(results, "Results list should never be null");
            
            for (EligibilityResult res : results) {
                count++;
                DecisionTrace trace = res.decisionTrace();
                assertNotNull(trace, "Trace must not be null for evaluated products");
                assertNotNull(trace.traceId(), "Trace ID must be set");
                assertNotNull(trace.executedAt(), "Execution timestamp must be set");
                assertEquals("1.0.0", trace.engineVersion(), "Engine version should match default");
                assertNotNull(trace.masterDataVersion(), "Master data version hash must be populated");
                assertTrue(trace.totalExecutionMillis() >= 0, "Execution timing should be recorded");

                // Validate snapshot
                EligibilityRequestSnapshot snapshot = trace.requestSnapshot();
                assertNotNull(snapshot, "Request snapshot must not be null");
                assertEquals(request.cibilScore(), snapshot.cibilScore());
                assertEquals(request.applicantAge(), snapshot.applicantAge());
                assertEquals(request.employmentType(), snapshot.employmentType());
                assertEquals(request.loanAmount().stripTrailingZeros(), snapshot.loanAmount().stripTrailingZeros());

                // Validate steps
                assertFalse(trace.steps().isEmpty(), "Steps list must not be empty");
                for (DecisionStep step : trace.steps()) {
                    assertNotNull(step.program(), "Step program must be set");
                    assertNotNull(step.status(), "Step status must be set");
                    assertNotNull(step.rules(), "Step rules must be non-null");
                    assertNotNull(step.formulas(), "Step formulas must be non-null");
                    
                    // Verify individual rule details
                    for (RuleEvaluation rule : step.rules()) {
                        assertNotNull(rule.ruleName(), "Rule name must be set");
                        assertNotNull(rule.status(), "Rule status must be set");
                        assertNotNull(rule.message(), "Rule message must be set");
                    }
                    
                    // Verify formulas
                    for (FormulaTrace formula : step.formulas()) {
                        assertNotNull(formula.formulaName(), "Formula name must be set");
                        assertNotNull(formula.expression(), "Formula expression must be set");
                        assertNotNull(formula.inputs(), "Formula inputs must be set");
                        assertNotNull(formula.output(), "Formula output must be set");
                    }
                }

                // Validate summary
                DecisionSummary summary = trace.summary();
                assertNotNull(summary, "Summary must be populated");
                assertNotNull(summary.finalStatus(), "Final status must be set");
                if (summary.finalStatus() == DecisionStatus.PASS) {
                    assertNotNull(summary.selectedProgram(), "Passed decision must have selected program");
                    assertTrue(summary.finalEligibleAmount().compareTo(BigDecimal.ZERO) >= 0);
                    assertTrue(summary.finalRoi().compareTo(BigDecimal.ZERO) > 0);
                }
            }
        }
        System.out.println("Successfully validated " + count + " product results across " + scenarios.size() + " profiles.");
    }

    private List<EligibilityRequest> generateGoldenScenarios() {
        List<EligibilityRequest> list = new ArrayList<>();
        
        Long[] lenders = {null, 1L, 2L, 3L, 4L}; // null maps to all lenders, others are specific IDs
        String[] loanTypes = {"HL", "LAP"};
        String[] empTypes = {"Salaried", "Self Employed", "Professional"};
        int[] cibilScores = {500, 650, 750};
        int[] ages = {21, 45, 62};
        BigDecimal[] loanAmounts = {new BigDecimal("2500000"), new BigDecimal("7500000")};
        BigDecimal[] propertyValues = {new BigDecimal("4000000"), new BigDecimal("10000000")};
        
        // Generate Cartesian product grid
        int idKey = 0;
        for (Long lender : lenders) {
            for (String loanType : loanTypes) {
                for (String empType : empTypes) {
                    for (int cibil : cibilScores) {
                        for (int age : ages) {
                            for (int aIdx = 0; aIdx < loanAmounts.length; aIdx++) {
                                idKey++;
                                BigDecimal loanAmt = loanAmounts[aIdx];
                                BigDecimal propVal = propertyValues[aIdx];
                                
                                IncomeComputationInput incomeInput = new IncomeComputationInput(
                                        "NIP",
                                        new BigDecimal("600000"),
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        BigDecimal.ZERO,
                                        List.of(),
                                        BigDecimal.ZERO,
                                        "",
                                        BigDecimal.ZERO,
                                        "",
                                        "",
                                        ""
                                );
                                
                                EligibilityRequest request = new EligibilityRequest(
                                        lender,
                                        loanType,
                                        cibil,
                                        age,
                                        empType,
                                        "Flat/Apartment/House",
                                        "Tier 1",
                                        loanAmt,
                                        propVal,
                                        180, // tenure
                                        new BigDecimal("50000"), // income
                                        BigDecimal.ZERO, // existing EMIs
                                        3, // biz age
                                        5, // work exp
                                        incomeInput,
                                        "idempotency-audit-" + idKey,
                                        3, // ITR years
                                        new BigDecimal("50000"), // gross
                                        "452001", // Indore area
                                        "Flat/Apartment/House",
                                        null
                                );
                                list.add(request);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}
