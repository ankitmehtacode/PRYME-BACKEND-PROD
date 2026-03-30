package com.pryme.Backend.recommendation;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/offers")
@RequiredArgsConstructor
public class PublicOfferController {

    private final LoanProductRepository loanProductRepository;

    @GetMapping("/hero")
    @Cacheable(cacheNames = "banks:recommendation", key = "'hero-offers'")
    public ResponseEntity<Map<String, Object>> heroOffers() {
        List<HeroOfferResponse> offers = loanProductRepository.findTop3ByBankIsActiveTrueOrderByInterestRateAsc()
                .stream()
                .map(this::toHeroOffer)
                .toList();

        return ResponseEntity.ok(Map.of("offers", offers));
    }

    private HeroOfferResponse toHeroOffer(LoanProduct product) {
        String bankName = product.getBank().getBankName();
        String displayType = product.getType().name();
        return new HeroOfferResponse(
                bankName.toLowerCase().replace(" ", "-") + "-" + product.getId().toString().substring(0, 8),
                product.getId(),
                product.getBank().getId(),
                bankName,
                product.getBank().getLogoUrl(),
                "Instant " + displayType + " limit up to INR 50,00,000",
                "• Rates from " + product.getInterestRate() + "%\n• Processing fee from " + product.getProcessingFee() + "%",
                "LIVE BANK OFFER",
                product.getInterestRate(),
                product.getProcessingFee(),
                displayType
        );
    }
}
