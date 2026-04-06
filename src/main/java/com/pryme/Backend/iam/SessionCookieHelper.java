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

    public SessionCookieHelper(
            @Value("${app.session.cookie-name:PRYME_SID}") String cookieName,
            @Value("${app.session.ttl-seconds:3600}") long ttlSeconds,
            @Value("${app.session.cookie-secure:true}") boolean secure
    ) {
        this.cookieName = cookieName;
        this.ttlSeconds = ttlSeconds;
        this.secure = secure;
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
     * SameSite  → Strict: Cookie NEVER sent on cross-origin requests (CSRF prevention)
     * Path      → Scoped to /api — frontend HTML/JS routes never see it
     */
    public ResponseCookie createSessionCookie(String sessionId) {
        return ResponseCookie.from(cookieName, sessionId)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api")
                .maxAge(Duration.ofSeconds(ttlSeconds))
                .build();
    }

    /**
     * 🧠 THE KILL COOKIE
     * Setting maxAge to 0 instructs the browser to immediately purge the cookie from its jar.
     */
    public ResponseCookie createClearCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api")
                .maxAge(0)
                .build();
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
                return (value != null && !value.isBlank()) ? value : null;
            }
        }
        return null;
    }
}
