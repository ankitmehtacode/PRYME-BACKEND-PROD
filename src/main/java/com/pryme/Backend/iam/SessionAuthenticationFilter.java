package com.pryme.Backend.iam;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    public SessionAuthenticationFilter(SessionManager sessionManager, UserRepository userRepository) {
        this.sessionManager = sessionManager;
        this.userRepository = userRepository;
    }

    // 🧠 THE FAST-LANE BYPASS: Explicitly skip token processing for public endpoints.
    // This prevents bad/expired browser tokens from crashing the Sign Up or Login processes.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/v1/auth/") ||
                path.startsWith("/api/v1/public/") ||
                path.startsWith("/api/v1/leads") ||
                path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                String token = header.substring(7);
                SessionRecord session = sessionManager.validate(token);

                if (session != null) {
                    userRepository.findById(session.userId()).ifPresent(user -> {
                        var auth = new UsernamePasswordAuthenticationToken(
                                user.getId(),
                                token,
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            } catch (Exception e) {
                // 🧠 THE BLAST SHIELD: Safely catch any token parsing errors (Expired, Malformed).
                // If we don't catch this, it bypasses the GlobalExceptionHandler and throws a hard 403!
                log.warn("Invalid or expired session token presented. Safely demoting request. Error: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}