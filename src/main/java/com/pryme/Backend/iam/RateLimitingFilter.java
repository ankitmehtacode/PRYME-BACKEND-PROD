ppackage com.pryme.Backend.iam;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
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

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ProxyManager<byte[]> proxyManager;

    public RateLimitingFilter(ProxyManager<byte[]> proxyManager) {
        this.proxyManager = proxyManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = extractClientIp(request);
        String requestURI = request.getRequestURI();

        Bucket bucket;

        if (requestURI.equals("/api/v1/auth/me") || requestURI.equals("/api/v1/auth/refresh")) {
            // ✅ SESSION CHECK: High limit — frontend polls this frequently
            // 300 requests per minute = 5 per second, plenty for normal use
            bucket = proxyManager.builder().build(
                    (ipAddress + "_session").getBytes(), this::createSessionBucketConfig);

        } else if (requestURI.startsWith("/api/v1/auth/login") || requestURI.startsWith("/api/v1/auth/register")) {
            // 🔒 STRICT BRUTE FORCE SHIELD: 10 attempts per minute for login/register only
            bucket = proxyManager.builder().build(
                    (ipAddress + "_auth").getBytes(), this::createAuthBucketConfig);

        } else if (requestURI.startsWith("/api/v1/auth")) {
            // 🔒 OTHER AUTH ENDPOINTS: 30 per minute
            bucket = proxyManager.builder().build(
                    (ipAddress + "_auth_other").getBytes(), this::createAuthOtherBucketConfig);

        } else if (requestURI.startsWith("/api/v1/leads")) {
            // 📋 LEADS: 15 per minute to stop DB spam
            bucket = proxyManager.builder().build(
                    (ipAddress + "_leads").getBytes(), this::createLeadBucketConfig);

        } else {
            // 🌐 GLOBAL: 200 per minute for standard browsing
            bucket = proxyManager.builder().build(
                    (ipAddress + "_global").getBytes(), this::createGlobalBucketConfig);
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("🛡️ API Fortress: Rate limit triggered for IP {} on URI {}", ipAddress, requestURI);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Too many requests. Please wait a moment before trying again.\", " +
                            "\"retryAfter\": 60}");
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // ✅ FIX: Only trust 10.x and 192.168.x as internal proxies
        // 172.x is excluded because Docker uses 172.x internally — treating it
        // as a trusted proxy causes ALL Docker users to share one IP
        boolean isTrustedProxy = remoteAddr.startsWith("10.") ||
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
                    return ip.split(",")[0].trim();
                }
            }
        }

        return remoteAddr;
    }

    // 🔒 Login/Register: 10 per minute (brute force protection)
    private BucketConfiguration createAuthBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillIntervally(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // ✅ /auth/me and /auth/refresh: 300 per minute (session checks)
    private BucketConfiguration createSessionBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(300)
                        .refillIntervally(300, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // Other /auth/** endpoints: 30 per minute
    private BucketConfiguration createAuthOtherBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(30)
                        .refillIntervally(30, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // 📋 Leads: 15 per minute
    private BucketConfiguration createLeadBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(15)
                        .refillIntervally(15, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // 🌐 Global: 200 per minute
    private BucketConfiguration createGlobalBucketConfig() {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(200)
                        .refillIntervally(200, Duration.ofMinutes(1))
                        .build())
                .build();
    }
}