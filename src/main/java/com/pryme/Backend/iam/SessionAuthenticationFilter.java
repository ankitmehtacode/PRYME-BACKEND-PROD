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

    public SessionAuthenticationFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // 🧠 THE FAST-LANE BYPASS
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/api/v1/public/") ||
                path.startsWith("/api/v1/leads") ||
                path.startsWith("/actuator/"); // 🧠 Added Actuator for KVM2 Health Checks
        // 🧠 REMOVED: /h2-console (We are fully PostgreSQL now)
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ") || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            // 🧠 160 IQ FIX 2: O(1) Memory Validation
            SessionRecord session = sessionManager.validate(token);

            if (session != null) {
                // 🧠 160 IQ FIX 3: Hydrate directly from Cache. NO DATABASE HITS.
                // (Assuming a standard ROLE_USER. If you need dynamic roles, add them to SessionRecord!)
                var auth = new UsernamePasswordAuthenticationToken(
                        session.getUserId(), // Principal
                        null,             // Credentials
                        List.of(new SimpleGrantedAuthority("ROLE_USER")) // Authorities
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
