package com.pryme.Backend.recommendation;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

import com.pryme.Backend.eligibility.service.FinancialComputationEngine;

@RestController
@RequestMapping("/api/v1/public/banks")
@RequiredArgsConstructor
@Validated
public class BankRecommendationController {

    private final BankRecommendationService bankRecommendationService;
    private final FinancialComputationEngine computationEngine;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping("/recommendation")
    public List<BankRecommendationResponse> getRecommendations(
            @RequestParam @NotNull BigDecimal salary,
            @RequestParam @NotNull @com.pryme.Backend.common.validation.ValidCibilScore Integer cibil
    ) {
        return bankRecommendationService.recommend(salary, cibil)
                .stream()
                .map(product -> toResponse(product, salary, cibil))
                .toList();
    }

    private BankRecommendationResponse toResponse(
            LoanProduct product,
            BigDecimal salary,
            Integer cibil
    ) {
        BigDecimal resolvedLoginFee = computationEngine.resolveLoginFee(product, salary);
        return new BankRecommendationResponse(
                product.getId(),
                product.getLenderId(),
                product.getLenderName(),
                product.getRoi(),
                product.getProcessingFee(),
                resolvedLoginFee,
                product.getLoanType(),
                bankRecommendationService.fitScore(product, salary, cibil),
                product.getAdminFee(),
                product.getInsuranceCharges(),
                product.getLegalTechnicalCharges(),
                product.getOtherExpense(),
                product.getStampDuties(),
                product.getPrepaymentCharges(),
                product.getForeclosureCharges()
        );
    }
}
