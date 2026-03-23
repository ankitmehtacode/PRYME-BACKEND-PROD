package com.pryme.Backend.iam;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // 🧠 BACKED BY CAFFEINE: Extremely fast in-memory eviction
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = extractClientIp(request);
        String requestURI = request.getRequestURI();

        // 🧠 DYNAMIC ROUTING RULES
        Bucket bucket;
        if (requestURI.startsWith("/api/v1/auth")) {
            // STRICT SHIELD: 5 requests per minute for Logins/Signups to prevent brute force
            bucket = bucketCache.get(ipAddress + "_auth", key -> createAuthBucket());
        } else if (requestURI.startsWith("/api/v1/leads")) {
            // PROGRESSIVE CAPTURE SHIELD: 15 requests per minute to stop DB spam
            bucket = bucketCache.get(ipAddress + "_leads", key -> createLeadBucket());
        } else {
            // GLOBAL SHIELD: 100 requests per minute for standard browsing (Banks, Calculators)
            bucket = bucketCache.get(ipAddress + "_global", key -> createGlobalBucket());
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("🛡️ API Fortress: Rate limit triggered for IP {} on URI {}", ipAddress, requestURI);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. API Fortress shield activated. Please wait 60 seconds.\"}");
        }
    }

    // ==========================================
    // 🧠 FAILPROOF IP EXTRACTOR (CLOUDFLARE SAFE)
    // ==========================================
    private String extractClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "CF-Connecting-IP", // Cloudflare
                "X-Forwarded-For",  // Standard Load Balancers
                "X-Real-IP"         // NGINX
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs. The first one is the true client.
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    private Bucket createAuthBucket() {
        return Bucket.builder().addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)))).build();
    }

    private Bucket createLeadBucket() {
        return Bucket.builder().addLimit(Bandwidth.classic(15, Refill.intervally(15, Duration.ofMinutes(1)))).build();
    }

    private Bucket createGlobalBucket() {
        return Bucket.builder().addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)))).build();
    }
}
