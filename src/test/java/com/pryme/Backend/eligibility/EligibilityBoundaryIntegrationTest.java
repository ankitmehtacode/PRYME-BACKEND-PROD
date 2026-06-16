package com.pryme.Backend.eligibility;

import com.pryme.Backend.eligibility.dto.EligibilityRequest;
import com.pryme.Backend.eligibility.dto.EligibilityResult;
import com.pryme.Backend.eligibility.dto.IncomeComputationInput;
import com.pryme.Backend.eligibility.entity.EligibilityCondition;
import com.pryme.Backend.eligibility.repository.EligibilityConditionRepository;
import com.pryme.Backend.eligibility.service.EligibilityEngineService;
import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class EligibilityBoundaryIntegrationTest {

    @MockBean
    private RedisClient redisClient;

    @MockBean
    private ProxyManager<byte[]> proxyManager;

    @Autowired
    private EligibilityEngineService eligibilityEngineService;

    @Autowired
    private LoanProductRepository loanProductRepository;

    @Autowired
    private EligibilityConditionRepository eligibilityConditionRepository;

    @Test
    public void testICICIBoundaryEdgeCase() {
        // 1. Clear H2 tables to start fresh
        eligibilityConditionRepository.deleteAll();
        loanProductRepository.deleteAll();

        // 2. Save ICICI Bank HL product
        LoanProduct product = LoanProduct.builder()
                .productCode("ICICI-HL-0001")
                .productName("ICICI Home Loan Standard")
                .loanType("HL")
                .lenderId(105L)
                .lenderName("ICICI Bank")
                .interestType("FLOATING")
                .minCibil(650).maxCibil(900)
                .roi(new BigDecimal("9.2500"))
                .processingFee(new BigDecimal("0.0050"))
                .minTenureMonths(12).maxTenureMonths(360)
                .minLoanAmount(new BigDecimal("100000.00"))
                .maxLoanAmount(new BigDecimal("999999999.00"))
                .active(true)
                .build();
        product = loanProductRepository.save(product);

        // 3. Save ICICI Bank eligibility conditions (NIP Slabs)
        // Bracket 1: min_income = 30000.00, foir_max = 0.50, ltv_allowed = 0.80
        EligibilityCondition cond1 = new EligibilityCondition();
        cond1.setProductId(product.getId());
        cond1.setProductCode(product.getProductCode());
        cond1.setEmploymentType("SALARIED_SEP");
        cond1.setSurrogate("NIP");
        cond1.setMinIncome(new BigDecimal("30000.00"));
        cond1.setFoirMax(new BigDecimal("0.5000"));
        cond1.setLtvAllowed(new BigDecimal("0.8000"));
        cond1.setMinAge(21);
        cond1.setMaxAge(65);
        cond1.setCibilMin(650);
        cond1.setPropertyType("RESIDENTIAL, COMMERCIAL, PLOT");
        cond1.setBankName("ICICI Bank");
        cond1.setLoanType("HL");
        cond1.setActive(true);
        eligibilityConditionRepository.save(cond1);

        // Bracket 2: min_income = 60001.00, foir_max = 0.60, ltv_allowed = 0.80
        EligibilityCondition cond2 = new EligibilityCondition();
        cond2.setProductId(product.getId());
        cond2.setProductCode(product.getProductCode());
        cond2.setEmploymentType("SALARIED_SEP");
        cond2.setSurrogate("NIP");
        cond2.setMinIncome(new BigDecimal("60001.00"));
        cond2.setFoirMax(new BigDecimal("0.6000"));
        cond2.setLtvAllowed(new BigDecimal("0.8000"));
        cond2.setMinAge(21);
        cond2.setMaxAge(65);
        cond2.setCibilMin(650);
        cond2.setPropertyType("RESIDENTIAL, COMMERCIAL, PLOT");
        cond2.setBankName("ICICI Bank");
        cond2.setLoanType("HL");
        cond2.setActive(true);
        eligibilityConditionRepository.save(cond2);

        // Bracket 3: min_income = 100001.00, foir_max = 0.65, ltv_allowed = 0.80
        EligibilityCondition cond3 = new EligibilityCondition();
        cond3.setProductId(product.getId());
        cond3.setProductCode(product.getProductCode());
        cond3.setEmploymentType("SALARIED_SEP");
        cond3.setSurrogate("NIP");
        cond3.setMinIncome(new BigDecimal("100001.00"));
        cond3.setFoirMax(new BigDecimal("0.6500"));
        cond3.setLtvAllowed(new BigDecimal("0.8000"));
        cond3.setMinAge(21);
        cond3.setMaxAge(65);
        cond3.setCibilMin(650);
        cond3.setPropertyType("RESIDENTIAL, COMMERCIAL, PLOT");
        cond3.setBankName("ICICI Bank");
        cond3.setLoanType("HL");
        cond3.setActive(true);
        eligibilityConditionRepository.save(cond3);

        // 4. Construct Request
        IncomeComputationInput incomeInput = new IncomeComputationInput(
                "NIP",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                List.of(),
                BigDecimal.ZERO,
                "",
                BigDecimal.ZERO,
                "",
                "ICICI Bank",
                "HL"
        );

        EligibilityRequest request = new EligibilityRequest(
                105L,                          // lenderId
                "HOME_LOAN",                   // loanType
                750,                           // cibilScore
                35,                            // applicantAge
                "SALARIED",                    // employmentType
                "Ready Built Property",        // propertyType
                "TIER_1",                      // cityTier
                new BigDecimal("3000000.50"),  // loanAmount
                new BigDecimal("4000000.00"),  // propertyValue
                240,                           // requestedTenureMonths
                new BigDecimal("60000.01"),    // monthlyIncome
                BigDecimal.ZERO,               // existingEmiTotal
                0,                             // businessAgeYears
                5,                             // workExpYears
                incomeInput,                   // incomeComputationInput
                "boundary-test-idemp",         // idempotencyKey
                3,                             // itrYearsAvailable
                new BigDecimal("60000.01"),    // grossMonthlyIncome
                "452001",                      // pinCode (Indore)
                null,
                null
        );

        // 5. Evaluate
        List<EligibilityResult> results = eligibilityEngineService.evaluate(request);

        // 6. Assert and print output
        assertFalse(results.isEmpty(), "Results should not be empty");
        EligibilityResult res = results.get(0);
        System.out.println("=== ELIGIBILITY RESULTS ===");
        System.out.println("Product Code: " + res.productCode());
        System.out.println("Lender Name: " + res.productName());
        System.out.println("Is Eligible: " + res.isEligible());
        System.out.println("Resolved Income: " + res.computedMonthlyIncome());
        System.out.println("LTV: " + res.ltv());
        System.out.println("FOIR: " + res.effectiveFoir());
        System.out.println("Rejection Reasons: " + res.rejectionReasons());

        assertTrue(res.isEligible(), "Applicant should be eligible");
        assertEquals(0, new BigDecimal("0.8000").compareTo(res.ltv()), "LTV should be 80% (0.80)");
        assertEquals(0, new BigDecimal("0.6000").compareTo(res.effectiveFoir()), "FOIR should be 60% (0.60)");
    }
}
