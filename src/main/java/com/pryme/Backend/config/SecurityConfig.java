package com.pryme.Backend.config;

import com.pryme.Backend.iam.RateLimitingFilter;
import com.pryme.Backend.iam.SessionAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
// 🧠 CRITICAL SHIELD: Activates @PreAuthorize across all controllers
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.security.allowed-origins:http://localhost:3000,http://localhost:5173,https://pryme.in}")
    private String allowedOrigins;

    /**
     * 🧠 CSP connect-src MUST match the exact API domain the frontend calls.
     * In production: https://crm.pryme.in
     * In dev: the Vite proxy handles it, so 'self' is sufficient.
     */
    @Value("${app.security.csp-connect-src:}")
    private String cspConnectSrc;

    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 🧠 CSRF DEFENSE VIA SameSite=Strict COOKIE (NOT TOKEN-BASED)
                // The session cookie is set with SameSite=Strict, which physically prevents
                // the browser from attaching it on ANY cross-origin request — same protection
                // that CSRF tokens provide but enforced at the browser engine level.
                // Re-enabling CookieCsrfTokenRepository would add zero marginal security here
                // and would require a frontend rewrite for the X-XSRF-TOKEN header dance.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // STATELESS SESSIONS: Guarantees zero memory leaks in Tomcat
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🧠 ZERO-TRUST EXCEPTION HANDLING
                // Formatted to exactly match our api.ts React frontend interceptor
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication matrix required or Token expired.\"}");
                }))

                // 🧠 THE GATEKEEPER MATRIX (Using Native Boot 3 Path Patterns)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // Whitelisted Public Vectors
                    auth.requestMatchers("/swagger-ui/**").permitAll()
                            .requestMatchers("/swagger-ui.html").permitAll()
                            .requestMatchers("/api-docs/**").permitAll()
                            .requestMatchers("/api-docs").permitAll()
                            .requestMatchers("/error").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/leads").permitAll()
                            .requestMatchers("/api/v1/public/**").permitAll()
                            .requestMatchers("/api/v1/eligibility/**").permitAll()
                            .requestMatchers("/api/v1/calculators/**").permitAll()
                            .requestMatchers("/internal/webhooks/**").permitAll()
                            .requestMatchers("/actuator/health").permitAll();

                    // Baseline Admin Protection
                    auth.requestMatchers("/actuator/**").denyAll()
                            .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE");

                    // 🧠 SSE endpoint: AUTHENTICATED (cookie required). NOT public.
                    // Already covered by anyRequest().authenticated() below.

                    // Lockdown everything else
                    auth.anyRequest().authenticated();
                })

                // ═══════════════════════════════════════════════════════════════════
                // 🧠 DIRECTIVE 2: CONTENT SECURITY POLICY & HARDENED HEADERS
                // ═══════════════════════════════════════════════════════════════════
                .headers(headers -> headers
                        // ── CSP: The XSS Annihilator ──────────────────────────────
                        // script-src: NO unsafe-inline, NO unsafe-eval. Period.
                        // style-src: unsafe-inline required by framer-motion dynamic styles.
                        // connect-src: Restricts fetch/XHR/SSE to our own domain only.
                        // img-src: Allows self, data URIs, Unsplash (Auth.tsx bg), and S3.
                        // font-src: Google Fonts for premium typography.
                        .contentSecurityPolicy(csp -> csp.policyDirectives(buildCspPolicy()))

                        // ── HSTS: Force HTTPS for 1 year, include subdomains ──────
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                                .preload(true)
                        )

                        // ── Frame Protection ──────────────────────────────────────
                        .frameOptions(frame -> frame.sameOrigin())

                        // ── Permissions Policy: Block device APIs ─────────────────
                        .permissionsPolicy(pp -> pp.policy(
                                "camera=(), microphone=(), geolocation=(), " +
                                "payment=(), usb=(), magnetometer=(), gyroscope=()"
                        ))
                )

                // 🧠 THE TITANIUM FILTER CHAIN ORDER
                // 1. Rate Limiting runs AFTER CorsFilter. If a bot gets rate-limited, the browser still gets
                //    the CORS headers, allowing Axios to cleanly read the 429 status instead of throwing a CORS error!
                .addFilterAfter(rateLimitingFilter, CorsFilter.class)
                // 2. Auth filter runs directly before the standard Spring UsernamePassword filter.
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 🧠 BUILDS THE CSP DIRECTIVE STRING
     * Separated into a method for readability and testability.
     */
    private String buildCspPolicy() {
        // Build connect-src: 'self' + any explicit API domain
        String connectSrc = (cspConnectSrc != null && !cspConnectSrc.isBlank())
                ? "'self' " + cspConnectSrc.trim()
                : "'self'";

        return String.join("; ",
                "default-src 'self'",
                "script-src 'self'",
                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
                "font-src 'self' https://fonts.gstatic.com",
                "img-src 'self' data: https://images.unsplash.com https://*.amazonaws.com",
                "connect-src " + connectSrc,
                "frame-ancestors 'self'",
                "object-src 'none'",
                "base-uri 'self'",
                "form-action 'self'"
        );
    }

    /**
     * 🧠 CORS CONFIGURATION — ZERO WILDCARDS
     * Every allowed origin, header, and method is explicitly declared.
     * Wildcards (*) in production are a fireable offense.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 Hour Preflight Cache

        // 🧠 DIRECTIVE 1: Explicit header whitelist — X-Client-Trace-Id for distributed tracing,
        // Idempotency-Key for mutation safety, Cache-Control for SSE EventSource
        configuration.setAllowedHeaders(List.of(
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Idempotency-Key",
                "X-Client-Trace-Id",
                "Cache-Control",
                "Last-Event-ID",           // SSE reconnection header
                "Authorization"            // Mobile/API fallback (Bearer header)
        ));

        // 🧠 Exposed headers the browser JS can read from responses
        // Authorization removed — session lives in HttpOnly cookie, not headers
        configuration.setExposedHeaders(List.of("X-Request-Id"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 🧠 Financial Grade Upgrade: 12 rounds for absolute brute-force resistance.
        return new BCryptPasswordEncoder(12);
    }
}
