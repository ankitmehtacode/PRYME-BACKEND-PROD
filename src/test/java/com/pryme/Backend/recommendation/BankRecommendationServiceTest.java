package com.pryme.Backend.recommendation;

import com.pryme.Backend.bankconfig.Bank;
import com.pryme.Backend.loanproduct.LoanProduct;
import com.pryme.Backend.loanproduct.LoanProductRepository;
import com.pryme.Backend.loanproduct.LoanProductType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(BankRecommendationService.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BankRecommendationServiceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BankRecommendationService bankRecommendationService;

    @Autowired
    private LoanProductRepository loanProductRepository;

    @Test
    void recommend_returnsOnlyProductsMatchingSalaryCibilAndActiveBank() {
        Bank activeBank = new Bank(null, "Active Bank", "/logos/a.png", true);
        Bank inactiveBank = new Bank(null, "Inactive Bank", "/logos/b.png", false);
        entityManager.persist(activeBank);
        entityManager.persist(inactiveBank);

        LoanProduct eligible = LoanProduct.builder()
                .bank(activeBank)
                .minSalary(new BigDecimal("50000.00"))
                .minCibil(700)
                .interestRate(new BigDecimal("8.75"))
                .processingFee(new BigDecimal("1.25"))
                .type(LoanProductType.HOME)
                .build();

        LoanProduct lowCibilProduct = LoanProduct.builder()
                .bank(activeBank)
                .minSalary(new BigDecimal("40000.00"))
                .minCibil(780)
                .interestRate(new BigDecimal("9.10"))
                .processingFee(new BigDecimal("1.50"))
                .type(LoanProductType.PERSONAL)
                .build();

        LoanProduct inactiveBankProduct = LoanProduct.builder()
                .bank(inactiveBank)
                .minSalary(new BigDecimal("30000.00"))
                .minCibil(650)
                .interestRate(new BigDecimal("10.50"))
                .processingFee(new BigDecimal("2.00"))
                .type(LoanProductType.BUSINESS)
                .build();

        loanProductRepository.saveAll(List.of(eligible, lowCibilProduct, inactiveBankProduct));

        List<LoanProduct> recommendations = bankRecommendationService.recommend(new BigDecimal("60000.00"), 730);

        assertThat(recommendations)
                .hasSize(1)
                .extracting(p -> p.getBank().getBankName())
                .containsExactly("Active Bank");
    }

    @Test
    void recommend_ordersByFitScoreThenPricing() {
        Bank bank = new Bank(null, "Fit Bank", "/logos/f.png", true);
        entityManager.persist(bank);

        LoanProduct highRate = LoanProduct.builder()
                .bank(bank)
                .minSalary(new BigDecimal("30000.00"))
                .minCibil(650)
                .interestRate(new BigDecimal("12.50"))
                .processingFee(new BigDecimal("2.50"))
                .type(LoanProductType.PERSONAL)
                .build();

        LoanProduct balanced = LoanProduct.builder()
                .bank(bank)
                .minSalary(new BigDecimal("50000.00"))
                .minCibil(700)
                .interestRate(new BigDecimal("8.75"))
                .processingFee(new BigDecimal("1.00"))
                .type(LoanProductType.HOME)
                .build();

        loanProductRepository.saveAll(List.of(highRate, balanced));

        List<LoanProduct> recommendations = bankRecommendationService.recommend(new BigDecimal("85000.00"), 780);

        assertThat(recommendations)
                .hasSize(2)
                .extracting(LoanProduct::getInterestRate)
                .containsExactly(new BigDecimal("8.75"), new BigDecimal("12.50"));
    }
}
