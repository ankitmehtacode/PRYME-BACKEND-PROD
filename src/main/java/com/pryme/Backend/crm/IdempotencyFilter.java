package com.pryme.Backend.crm;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AntPathRequestMatcher matcher = new AntPathRequestMatcher("/api/v1/public/leads", HttpMethod.POST.name());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !matcher.matches(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyHeader = request.getHeader("Idempotency-Key");
        if (!StringUtils.hasText(idempotencyHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        String keyHash = sha256Hex(idempotencyHeader);

        try {
            idempotencyKeyRepository.insertInProgressKey(keyHash);
        } catch (DataIntegrityViolationException ex) {
            IdempotencyKey existing = idempotencyKeyRepository.findByKeyHash(keyHash).orElse(null);
            if (existing != null) {
                if ("IN_PROGRESS".equals(existing.getStatus())) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Concurrent request in progress\"}");
                    return;
                } else if ("COMPLETED".equals(existing.getStatus())) {
                    response.setStatus(existing.getHttpStatus());
                    response.setContentType("application/json");
                    response.getWriter().write(existing.getResponseBody());
                    return;
                }
            }
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(request, wrappedResponse);

        String responseBody = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
        int responseStatus = wrappedResponse.getStatus();

        idempotencyKeyRepository.findByKeyHash(keyHash).ifPresent(key -> {
            key.setStatus("COMPLETED");
            key.setResponseBody(responseBody);
            key.setHttpStatus(responseStatus);
            idempotencyKeyRepository.save(key);
        });

        wrappedResponse.copyBodyToResponse();
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

@Configuration
class IdempotencyFilterConfiguration {

    @Bean
    IdempotencyFilter idempotencyFilter(IdempotencyKeyRepository idempotencyKeyRepository) {
        return new IdempotencyFilter(idempotencyKeyRepository);
    }

    @Bean
    FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(IdempotencyFilter idempotencyFilter) {
        FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(idempotencyFilter);
        registration.setOrder(SecurityProperties.DEFAULT_FILTER_ORDER + 1);
        registration.addUrlPatterns("/api/v1/public/leads");
        return registration;
    }
}
