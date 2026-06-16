package com.pryme.Backend.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HeroOfferConfigRepository extends JpaRepository<HeroOfferConfig, UUID> {
    List<HeroOfferConfig> findAllByActiveTrueOrderByOrderIndexAsc();
    List<HeroOfferConfig> findAllByOrderByOrderIndexAsc();
}
