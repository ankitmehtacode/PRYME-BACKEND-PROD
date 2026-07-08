package com.pryme.Backend.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/offers/rewards")
@RequiredArgsConstructor
public class PublicProductRewardController {

    private final ProductRewardRepository productRewardRepository;

    @GetMapping
    public ResponseEntity<List<ProductReward>> getAllRewards() {
        return ResponseEntity.ok(productRewardRepository.findAll());
    }
}
