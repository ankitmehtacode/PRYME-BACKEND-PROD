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

@RestController
@RequestMapping("/api/v1/public/banks")
@RequiredArgsConstructor
@Validated
public class BankRecommendationController {

    private final BankRecommendationService bankRecommendationService;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping("/recommendation")
    public List<BankRecommendationResponse> getRecommendations(
            @RequestParam @NotNull BigDecimal salary,
            @RequestParam @NotNull @Min(300) @Max(900) Integer cibil
    ) {
        return bankRecommendationService.recommend(salary, cibil)
                .stream()
                .map(product -> toResponse(product, salary, cibil, bankRecommendationService))
                .toList();
    }

    private static BankRecommendationResponse toResponse(
            LoanProduct product,
            BigDecimal salary,
            Integer cibil,
            BankRecommendationService service
    ) {
        return new BankRecommendationResponse(
                product.getId(),
                product.getLenderId(),
                product.getLenderName(),
                product.getRoi(),
                product.getProcessingFee(),
                product.getLoanType(),
                service.fitScore(product, salary, cibil)
        );
    }
}
