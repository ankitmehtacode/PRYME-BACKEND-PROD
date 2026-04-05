package com.pryme.Backend.iam;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
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

    // 🧠 BACKED BY REDIS: Distributed Coordination across pods
    private final ProxyManager<byte[]> proxyManager;

    public RateLimitingFilter(ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = extractClientIp(request);
        String requestURI = request.getRequestURI();

        // 🧠 DYNAMIC ROUTING RULES
        Bucket bucket;
        if (requestURI.startsWith("/api/v1/auth")) {
            // STRICT SHIELD: 5 requests per minute for Logins/Signups to prevent brute force
            bucket = proxyManager.builder().build((ipAddress + "_auth").getBytes(), this::createAuthBucketConfig);
        } else if (requestURI.startsWith("/api/v1/leads")) {
            // PROGRESSIVE CAPTURE SHIELD: 15 requests per minute to stop DB spam
            bucket = proxyManager.builder().build((ipAddress + "_leads").getBytes(), this::createLeadBucketConfig);
        } else {
            // GLOBAL SHIELD: 100 requests per minute for standard browsing (Banks, Calculators)
            bucket = proxyManager.builder().build((ipAddress + "_global").getBytes(), this::createGlobalBucketConfig);
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
        String remoteAddr = request.getRemoteAddr();
        
        // 🧠 160 IQ FIX: Prevent IP spoofing bypass
        // Blindly trusting headers allows attackers to reset their rate limit.
        // We only parse Forwarded/CF headers if the TCP connection physically originates from an internal Load Balancer or VPC.
        boolean isTrustedProxy = remoteAddr.startsWith("10.") || 
                                 remoteAddr.startsWith("172.") || 
                                 remoteAddr.startsWith("192.168.") || 
                                 remoteAddr.equals("127.0.0.1") || 
                                 remoteAddr.equals("0:0:0:0:0:0:0:1");

        if (isTrustedProxy) {
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
        }
        return remoteAddr;
    }

    private BucketConfiguration createAuthBucketConfig() {
        return BucketConfiguration.builder().addLimit(Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build()).build();
    }

    private BucketConfiguration createLeadBucketConfig() {
        return BucketConfiguration.builder().addLimit(Bandwidth.builder().capacity(15).refillIntervally(15, Duration.ofMinutes(1)).build()).build();
    }

    private BucketConfiguration createGlobalBucketConfig() {
        return BucketConfiguration.builder().addLimit(Bandwidth.builder().capacity(100).refillIntervally(100, Duration.ofMinutes(1)).build()).build();
    }
}
