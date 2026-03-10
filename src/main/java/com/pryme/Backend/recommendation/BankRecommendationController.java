package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.LoanProduct;
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

    @GetMapping("/recommendation")
    public List<BankRecommendationResponse> getRecommendations(
            @RequestParam @NotNull BigDecimal salary,
            @RequestParam @NotNull @Min(300) @Max(900) Integer cibil
    ) {
        return bankRecommendationService.recommend(salary, cibil)
                .stream()
                .map(BankRecommendationController::toResponse)
                .toList();
    }

    private static BankRecommendationResponse toResponse(LoanProduct product) {
        return new BankRecommendationResponse(
                product.getId(),
                product.getBank().getId(),
                product.getBank().getBankName(),
                product.getBank().getLogoUrl(),
                product.getInterestRate(),
                product.getProcessingFee(),
                product.getType().name()
        );
    }
}
