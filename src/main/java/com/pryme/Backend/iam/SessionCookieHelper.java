package com.pryme.Backend.iam;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 🧠 SINGLE-RESPONSIBILITY COOKIE ENGINE
 * Encapsulates all HttpOnly session cookie operations.
 * Keeps controllers and filters clean. One class owns the cookie contract.
 */
@Component
public class SessionCookieHelper {

    private final String cookieName;
    private final long ttlSeconds;
    private final boolean secure;
    private final String domain;
    private final String sameSite;
    private final boolean isDevEnvironment;

    public SessionCookieHelper(
            @Value("${app.session.cookie-name:PRYME_SID}") String cookieName,
            @Value("${app.session.ttl-seconds:3600}") long ttlSeconds,
            @Value("${app.session.cookie-secure:true}") boolean secure,
            @Value("${app.session.cookie-domain:}") String domain,
            @Value("${app.session.cookie-same-site:Lax}") String sameSite,
            org.springframework.core.env.Environment env
    ) {
        this.cookieName = cookieName;
        this.ttlSeconds = ttlSeconds;
        this.domain = (domain != null && !domain.isBlank()) ? domain.trim() : null;
        this.sameSite = sameSite;
        
        // 🧠 THE 1% FIX: Auto-detect dev profile to prevent silent cookie drops on HTTP
        String activeProfile = env.getProperty("spring.profiles.active", "");
        boolean hasDevInArray = java.util.Arrays.asList(env.getActiveProfiles()).contains("dev");
        this.isDevEnvironment = hasDevInArray || activeProfile.contains("dev");
        this.secure = this.isDevEnvironment ? false : secure; 
    }

    /**
     * Returns the configured session TTL in seconds.
     * Used by AuthController to align cookie MaxAge with DB session expiry.
     */
    public long getTtlSeconds() {
        return ttlSeconds;
    }

    /**
     * 🧠 BUILD THE FORTRESS COOKIE
     * HttpOnly  → Invisible to document.cookie / XSS payloads
     * Secure    → Transmitted only over HTTPS (prevents MITM sniffing on HTTP)
     * SameSite  → Lax (default): Safe for local dev cross-port proxying (8081→8082).
     *              Production can override to Strict via app.session.cookie-same-site=Strict.
     *              Lax still blocks cross-site POST requests (CSRF) while allowing top-level
     *              navigations to carry the cookie.
     * Path      → Scoped to /api — frontend HTML/JS routes never see it
     * Domain    → Explicit when configured (multi-subdomain); implicit otherwise (single-origin)
     */
    public ResponseCookie createSessionCookie(String sessionId) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, sessionId)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api")
                .maxAge(Duration.ofSeconds(ttlSeconds));

        if (domain != null) {
            builder.domain(domain);
        }

        return builder.build();
    }

    /**
     * 🧠 THE KILL COOKIE
     * Setting maxAge to 0 instructs the browser to immediately purge the cookie from its jar.
     */
    public ResponseCookie createClearCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api")
                .maxAge(0);

        if (domain != null) {
            builder.domain(domain);
        }

        return builder.build();
    }

    /**
     * 🧠 COOKIE EXTRACTION (Used by SessionAuthenticationFilter)
     * Returns the session ID from the HttpOnly cookie, or null if absent.
     */
    public String extractSessionId(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                String value = cookie.getValue();
                return (value != null && !value.isBlank()) ? value.replace("\"", "") : null;
            }
        }
        return null;
    }
}
