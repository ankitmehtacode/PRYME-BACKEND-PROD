package com.pryme.Backend.recommendation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "product_rewards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReward {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String bank;

    @Column(name = "product_code", nullable = false, length = 100)
    private String productCode;

    @Column(name = "icon_type", nullable = false, length = 50)
    private String iconType;

    @Column(name = "reward_text", nullable = false, length = 255)
    private String rewardText;

    @Column(name = "button_design", nullable = false, length = 100)
    private String buttonDesign = "ocean-blue";

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
