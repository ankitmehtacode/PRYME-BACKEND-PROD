package com.pryme.Backend.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisClient redisClient() {
        RedisURI uri = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .build();
        return RedisClient.create(uri);
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        // 🧠 SECURE CLUSTER STATE: This allows all Spring backend nodes to sync their Bucket tokens.
        // Distributed state uses microsecond-resolution CAS (Compare-And-Swap) logic over Redis.
        return LettuceBasedProxyManager.builderFor(redisClient)
                .withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofHours(1)))
                .build();
    }
}
