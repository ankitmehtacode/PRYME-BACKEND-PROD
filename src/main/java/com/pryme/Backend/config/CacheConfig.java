package com.pryme.Backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

import java.time.Duration;

import java.util.concurrent.TimeUnit;


@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() 
    {


        CaffeineCacheManager manager = new CaffeineCacheManager(
                "banks:all",
                "banks:recommendation",
                "banks:partners",
                "auth:sessions",
                "content:testimonials"
        );

        manager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(512)
                .maximumSize(20_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());



        CaffeineCacheManager manager = new CaffeineCacheManager("banks:all", "banks:recommendation", "banks:partners", "auth:sessions", "content:testimonials");
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(10, TimeUnit.MINUTES));

        return manager;
    }
}
