package com.pryme.Backend.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/offers/rewards")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminProductRewardController {

    private final ProductRewardRepository productRewardRepository;

    @GetMapping
    public ResponseEntity<List<ProductReward>> getAllRewards() {
        return ResponseEntity.ok(productRewardRepository.findAll());
    }

    @PostMapping
    @CacheEvict(cacheNames = "banks:recommendation", allEntries = true)
    public ResponseEntity<ProductReward> createReward(@RequestBody ProductReward reward) {
        reward.setId(null);
        return ResponseEntity.ok(productRewardRepository.save(reward));
    }

    @PutMapping("/{id}")
    @CacheEvict(cacheNames = "banks:recommendation", allEntries = true)
    public ResponseEntity<ProductReward> updateReward(@PathVariable UUID id, @RequestBody ProductReward updatedReward) {
        return productRewardRepository.findById(id).map(existing -> {
            existing.setBank(updatedReward.getBank());
            existing.setProductCode(updatedReward.getProductCode());
            existing.setIconType(updatedReward.getIconType());
            existing.setRewardText(updatedReward.getRewardText());
            if (updatedReward.getButtonDesign() != null) {
                existing.setButtonDesign(updatedReward.getButtonDesign());
            }
            if (updatedReward.getLogoUrl() != null) {
                existing.setLogoUrl(updatedReward.getLogoUrl());
            }
            return ResponseEntity.ok(productRewardRepository.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @CacheEvict(cacheNames = "banks:recommendation", allEntries = true)
    public ResponseEntity<Void> deleteReward(@PathVariable UUID id) {
        if (productRewardRepository.existsById(id)) {
            productRewardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
