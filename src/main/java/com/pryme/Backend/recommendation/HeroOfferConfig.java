package com.pryme.Backend.recommendation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hero_offer_configs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeroOfferConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String tag;

    @Column(nullable = false, length = 100)
    private String bank;

    @Column(name = "logo_type", length = 50)
    private String logoType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 500)
    private String highlights;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;

    @Column(name = "banner_image_url", length = 1024)
    private String bannerImageUrl;

    @Column(name = "hero_image_url", length = 1024)
    private String heroImageUrl;

    @Column(name = "target_url", length = 1024)
    private String targetUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
