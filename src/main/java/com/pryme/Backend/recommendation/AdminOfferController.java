package com.pryme.Backend.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/offers/hero")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminOfferController {

    private final HeroOfferConfigRepository heroOfferConfigRepository;

    @GetMapping
    public ResponseEntity<List<HeroOfferConfig>> getAllOffers() {
        return ResponseEntity.ok(heroOfferConfigRepository.findAllByOrderByOrderIndexAsc());
    }

    @PostMapping
    @CacheEvict(cacheNames = "banks:recommendation", allEntries = true)
    public ResponseEntity<HeroOfferConfig> createOffer(@RequestBody HeroOfferConfig offer) {
        // Enforce null id for creation
        offer.setId(null);
        return ResponseEntity.ok(heroOfferConfigRepository.save(offer));
    }

    @PutMapping("/{id}")
    @CacheEvict(cacheNames = "banks:recommendation", allEntries = true)
    public ResponseEntity<HeroOfferConfig> updateOffer(@PathVariable UUID id, @RequestBody HeroOfferConfig updatedOffer) {
        return heroOfferConfigRepository.findById(id).map(existing -> {
            existing.setTag(updatedOffer.getTag());
            existing.setBank(updatedOffer.getBank());
            existing.setLogoType(updatedOffer.getLogoType());
            existing.setTitle(updatedOffer.getTitle());
            existing.setHighlights(updatedOffer.getHighlights());
            existing.setActive(updatedOffer.isActive());
            existing.setOrderIndex(updatedOffer.getOrderIndex());
            existing.setBannerImageUrl(updatedOffer.getBannerImageUrl());
            existing.setHeroImageUrl(updatedOffer.getHeroImageUrl());
            existing.setTargetUrl(updatedOffer.getTargetUrl());
            return ResponseEntity.ok(heroOfferConfigRepository.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "banks:recommendation", allEntries = true)
    public ResponseEntity<Void> deleteOffer(@PathVariable UUID id) {
        if (heroOfferConfigRepository.existsById(id)) {
            heroOfferConfigRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
