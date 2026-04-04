package com.pryme.Backend.eligibility.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.time.Duration;

@Configuration
public class SpelSandboxConfig {

    @Bean
    Cache<String, Expression> spelExpressionCache() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(Duration.ofHours(1))
                .build();
    }

    @Bean
    SimpleEvaluationContext simpleSandboxEvaluationContext() {
        // Permitted: property access, map lookups, and safe instance method calls.
        // Blocked: type references, static method calls, reflection/new object construction.
        return SimpleEvaluationContext.forReadOnlyDataBinding()
                .withInstanceMethods()
                .build();
    }
}
