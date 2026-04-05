package com.pryme.Backend.recommendation;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final LoanProductRepository loanProductRepository;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping
    @Cacheable(cacheNames = "banks:recommendation", key = "'product-grid'")
    public ResponseEntity<Map<String, Object>> productGrid() {
        List<ProductCardResponse> cards = List.of(
                buildCard("PERSONAL", "PERSONAL LOAN", "CASHBACK", "/apply?type=personal", "148, 62%, 42%"),
                buildCard("BUSINESS", "BUSINESS LOAN", "LOWEST RATES", "/apply?type=business", "217, 91%, 60%"),
                buildCard("HOME", "HOME LOAN", "PRE-APPROVED", "/apply?type=home", "48, 100%, 50%"),
                buildCard("EDUCATION", "EDUCATION LOAN", "100% FUNDING", "/apply?type=education", "270, 70%, 60%")
        );

        return ResponseEntity.ok(Map.of("products", cards));
    }

    private ProductCardResponse buildCard(String loanType, String label, String tag, String href, String accent) {
        return loanProductRepository.findByLoanTypeAndActive(loanType, true)
                .stream()
                .min(Comparator.comparing(p -> p.getRoi() == null ? new BigDecimal("99.99") : p.getRoi()))
                .map(p -> new ProductCardResponse(
                        loanType.toLowerCase(),
                        label,
                        tag,
                        href,
                        accent,
                        p.getRoi(),
                        p.getProcessingFee()
                ))
                .orElseGet(() -> new ProductCardResponse(loanType.toLowerCase(), label, tag, href, accent, null, null));
    }
}
