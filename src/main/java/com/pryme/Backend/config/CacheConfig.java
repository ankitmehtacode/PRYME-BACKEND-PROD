package com.pryme.Backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    // ==========================================
    // 🧠 MULTI-TIERED L1 CACHE MATRIX
    // Prevents Heap memory exhaustion by applying specific
    // TTLs and Size limits to specific domains.
    // ==========================================
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();

        manager.setCaches(Arrays.asList(
                // 1. STATIC CONFIG DATA: Rarely changes. Tiny footprint. 24-Hour TTL.
                buildCache("banks:all", 200, Duration.ofHours(24)),
                buildCache("banks:partners", 100, Duration.ofHours(24)),

                // 2. DYNAMIC CONTENT: Changes occasionally. 1-Hour TTL.
                buildCache("content:testimonials", 500, Duration.ofHours(1)),

                // 3. COMPUTATIONAL RESULTS: Caches heavy CIBIL/Rule Engine outputs to save CPU.
                // Scaled to 5,000 to accommodate peak user traffic. 30-Min TTL.
                buildCache("banks:recommendation", 5000, Duration.ofMinutes(30)),

                // 4. 🧠 SECURITY MICRO-CACHING: The Top 1% Optimization
                // Max size 10,000. TTL: EXACTLY 5 SECONDS.
                // Why? If a user's React frontend fires 6 API calls at once on page load,
                // this cache absorbs 5 of them, dropping DB load by 83%.
                // But if an Admin bans them, the ban takes effect in maximum 5 seconds.
                buildCache("auth:sessions", 10000, Duration.ofSeconds(5))
        ));

        log.info("Titanium L1 Cache Matrix Initialized. Protection layers active.");
        return manager;
    }

    /**
     * 🧠 CACHE FACTORY
     * Generates heavily optimized Caffeine boundaries to protect JVM Heap space.
     */
    private CaffeineCache buildCache(String name, int maxSize, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .initialCapacity(Math.min(maxSize, 100)) // Don't pre-allocate massive arrays
                .maximumSize(maxSize)                    // Strict Hard Ceiling
                .expireAfterWrite(ttl)                   // Time-To-Live
                .recordStats()                           // Exposes hit/miss metrics to Spring Actuator (Prometheus/Grafana)
                .build());
    }
}