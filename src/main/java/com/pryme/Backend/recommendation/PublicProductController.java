package com.pryme.Backend.recommendation;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
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
    @Timed(value = "pryme.api.public.products", description = "Public product grid latency")
    public ResponseEntity<Map<String, Object>> productGrid() {
        List<String> loanTypes = List.of("PERSONAL", "BUSINESS", "HOME", "EDUCATION");
        Map<String, LoanProductSnapshot> bestByType = bestProductByLoanType(loanTypes);

        List<ProductCardResponse> cards = List.of(
                buildCard("PERSONAL", "PERSONAL LOAN", "CASHBACK", "/apply?type=personal", "148, 62%, 42%", bestByType),
                buildCard("BUSINESS", "BUSINESS LOAN", "LOWEST RATES", "/apply?type=business", "217, 91%, 60%", bestByType),
                buildCard("HOME", "HOME LOAN", "PRE-APPROVED", "/apply?type=home", "48, 100%, 50%", bestByType),
                buildCard("EDUCATION", "EDUCATION LOAN", "100% FUNDING", "/apply?type=education", "270, 70%, 60%", bestByType)
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(Map.of("products", cards));
    }

    private ProductCardResponse buildCard(
            String loanType,
            String label,
            String tag,
            String href,
            String accent,
            Map<String, LoanProductSnapshot> bestByType
    ) {
        LoanProductSnapshot best = bestByType.get(loanType);
        return new ProductCardResponse(
                loanType.toLowerCase(),
                label,
                tag,
                href,
                accent,
                best != null ? best.roi() : null,
                best != null ? best.processingFee() : null
        );
    }

    private Map<String, LoanProductSnapshot> bestProductByLoanType(List<String> loanTypes) {
        Map<String, LoanProductSnapshot> bestByType = new HashMap<>();
        loanProductRepository.findByLoanTypeInAndActive(loanTypes, true).forEach(product ->
                bestByType.merge(
                        product.getLoanType(),
                        new LoanProductSnapshot(product.getRoi(), product.getProcessingFee()),
                        this::preferLowerRoi
                )
        );
        return bestByType;
    }

    private LoanProductSnapshot preferLowerRoi(LoanProductSnapshot left, LoanProductSnapshot right) {
        BigDecimal leftRoi = left.roi() == null ? new BigDecimal("99.99") : left.roi();
        BigDecimal rightRoi = right.roi() == null ? new BigDecimal("99.99") : right.roi();
        return Comparator.<BigDecimal>naturalOrder().compare(leftRoi, rightRoi) <= 0 ? left : right;
    }

    private record LoanProductSnapshot(BigDecimal roi, BigDecimal processingFee) {}
}
