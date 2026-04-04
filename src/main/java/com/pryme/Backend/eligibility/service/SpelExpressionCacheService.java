package com.pryme.Backend.eligibility.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpelExpressionCacheService {

    private final Cache<String, Expression> spelExpressionCache;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    public Expression getOrCompile(String spelExpression) {
        return spelExpressionCache.get(spelExpression, parser::parseExpression);
    }
}
