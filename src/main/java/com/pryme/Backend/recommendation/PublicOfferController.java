package com.pryme.Backend.recommendation;

import io.swagger.v3.oas.annotations.Operation;

import com.pryme.Backend.loanproduct.entity.LoanProduct;
import com.pryme.Backend.loanproduct.repository.LoanProductRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/offers")
@RequiredArgsConstructor
public class PublicOfferController {

    private final LoanProductRepository loanProductRepository;
    private final HeroOfferConfigRepository heroOfferConfigRepository;

    @Operation(summary = "One-line description of this endpoint")
    @GetMapping("/hero")
    @Cacheable(cacheNames = "banks:recommendation", key = "'hero-offers'")
    @Timed(value = "pryme.api.public.hero_offers", description = "Public hero offers latency")
    public ResponseEntity<Map<String, Object>> heroOffers() {
        List<HeroOfferConfig> customConfigs = heroOfferConfigRepository.findAllByActiveTrueOrderByOrderIndexAsc();

        List<HeroOfferResponse> offers;
        if (!customConfigs.isEmpty()) {
            offers = customConfigs.stream()
                    .map(config -> new HeroOfferResponse(
                            String.valueOf(config.getId()),
                            null,
                            null,
                            config.getBank(),
                            config.getTitle(),
                            config.getHighlights(),
                            config.getTag(),
                            null,
                            null,
                            null,
                            null,
                            config.getLogoType(),
                            config.getBannerImageUrl(),
                            config.getHeroImageUrl(),
                            config.getTargetUrl()
                    ))
                    .toList();
        } else {
            offers = loanProductRepository.findTop3ByActiveTrueOrderByRoiAsc()
                    .stream()
                    .map(this::toHeroOffer)
                    .toList();
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic())
                .body(Map.of("offers", offers));
    }

    private HeroOfferResponse toHeroOffer(LoanProduct product) {
        String lenderName = product.getLenderName() != null ? product.getLenderName() : "unknown";
        String displayType = product.getLoanType() != null ? product.getLoanType() : "loan";
        String idStr = String.valueOf(product.getId());
        // Safe slug: lender-name-{id} — no substring crash regardless of ID length
        String slug = lenderName.toLowerCase().replace(" ", "-") + "-" + idStr;
        return new HeroOfferResponse(
                slug,
                product.getId(),
                product.getLenderId(),
                lenderName,
                "Instant " + displayType + " limit up to INR 50,00,000",
                "• Rates from " + product.getRoi() + "%",
                "LIVE BANK OFFER",
                product.getRoi(),
                null, // Processing fee is dynamic now
                product.getLoginFees() != null ? product.getLoginFees() : BigDecimal.ZERO,
                displayType,
                null,
                null,
                null,
                "/apply"
        );
    }
}
