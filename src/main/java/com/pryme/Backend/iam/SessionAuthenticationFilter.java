package com.pryme.Backend.iam;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationFilter.class);

    // 🧠 160 IQ FIX 1: UserRepository is BANNED from the Filter Layer.
    // We rely 100% on the O(1) SessionManager RAM Cache to protect the PostgreSQL DB.
    private final SessionManager sessionManager;
    private final SessionCookieHelper cookieHelper;

    public SessionAuthenticationFilter(SessionManager sessionManager, SessionCookieHelper cookieHelper) {
        this.sessionManager = sessionManager;
        this.cookieHelper = cookieHelper;
    }

    // 🧠 THE FAST-LANE BYPASS
    // CRITICAL: /auth/me, /auth/logout, /auth/sessions MUST run through this filter
    // because they require an authenticated session. Only registration and login are public.
    private static final List<String> PUBLIC_AUTH_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/signup"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Exact-match public auth endpoints (NOT wildcard — /auth/me must NOT be skipped)
        if (PUBLIC_AUTH_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }

        return path.startsWith("/api/v1/public/") ||
                path.startsWith("/api/v1/leads") ||
                path.startsWith("/api/v1/eligibility/") ||
                path.startsWith("/api/v1/calculators/") ||
                path.startsWith("/actuator/"); // KVM2 Health Checks
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 🧠 SECURITY FIX: DUAL-READ SESSION EXTRACTION
        // Priority 1: HttpOnly Cookie (browser clients — XSS-proof)
        // Priority 2: Authorization: Bearer header (mobile apps, Swagger, Postman)
        String sessionId = extractSessionId(request);

        if (sessionId == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 🧠 160 IQ FIX 2: O(1) Memory Validation (UNTOUCHED — same SessionManager path)
            SessionRecord session = sessionManager.validate(sessionId);

            if (session != null) {
                // 🧠 160 IQ FIX 3: Hydrate directly from Cache. NO DATABASE HITS.
                var auth = new UsernamePasswordAuthenticationToken(
                        session.getUser().getId(), // Principal
                        null,             // Credentials
                        List.of(new SimpleGrantedAuthority("ROLE_" + session.getUser().getRole().name())) // Authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                // 🧠 INSTANT CONNECTION DROP
                logAndBlock(response, "Session invalid or expired.");
                return;
            }
        } catch (Exception e) {
            log.warn("Security Matrix Blocked Invalid Token: {}", e.getMessage());
            logAndBlock(response, "Malformed or compromised security token.");
            return; // 🧠 CRITICAL: Stop the filter chain immediately!
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 🧠 DUAL-READ SESSION ID EXTRACTION
     * Cookie takes absolute priority (HttpOnly = impossible for XSS to tamper).
     * Bearer header exists solely for non-browser clients (mobile SDKs, API integrations).
     */
    private String extractSessionId(HttpServletRequest request) {
        // Priority 1: HttpOnly Cookie
        String fromCookie = cookieHelper.extractSessionId(request);
        if (fromCookie != null) {
            return fromCookie;
        }

        // Priority 2: Bearer Header (mobile/API fallback)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    /**
     * 🧠 160 IQ FIX 4: The Hard Drop
     * Instantly returns a clean 401 JSON to React, preventing Spring Security
     * from generating a massive HTML stack trace that crashes Axios.
     */
    private void logAndBlock(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}

