package com.pryme.Backend.config;

import com.pryme.Backend.iam.RateLimitingFilter;
import com.pryme.Backend.iam.SessionAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        // 🧠 FIX 1: Aligned with standard .env naming conventions to prevent injection
        // failure
        @Value("${ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,https://gopryme.tech}")
        private String allowedOrigins;

        @Value("${app.security.csp-connect-src:}")
        private String cspConnectSrc;

        private final SessionAuthenticationFilter sessionAuthenticationFilter;
        private final RateLimitingFilter rateLimitingFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                // 🧠 CORS must be the first line of defense
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        log.error("Security Entry Point: Unauthorized access to {}",
                                                                        request.getRequestURI());
                                                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                                        response.getWriter().write(
                                                                        "{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Authentication matrix required or Token expired.\"}");
                                                }))
                                .authorizeHttpRequests(auth -> {
                                        // 🧠 ASYNC DISPATCH: Tomcat re-dispatches SSE timeouts as ASYNC.
                                        // Without this, the AuthorizationFilter blocks them → Access Denied.
                                        auth.dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll();

                                        // 🧠 Explicit Preflight Handling
                                        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                                        // Whitelisted Public Vectors
                                        auth.requestMatchers(
                                                        "/swagger-ui/**", "/swagger-ui.html", "/api-docs/**",
                                                        "/api-docs",
                                                        "/error", "/api/v1/auth/**", "/api/v1/public/**",
                                                        "/api/v1/eligibility/**", "/api/v1/calculators/**",
                                                        "/api/v1/config/dictionaries",
                                                        "/internal/webhooks/**", "/actuator/health").permitAll();

                                        auth.requestMatchers(HttpMethod.POST, "/api/v1/leads").permitAll();

                                        // Baseline Admin Protection
                                        auth.requestMatchers("/actuator/**").denyAll()
                                                        .requestMatchers("/api/v1/admin/**")
                                                        .hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE");

                                        auth.anyRequest().authenticated();
                                })
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp.policyDirectives(buildCspPolicy()))
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)
                                                                .preload(true))
                                                .frameOptions(frame -> frame.sameOrigin())
                                                .permissionsPolicy(pp -> pp.policy(
                                                                "camera=(), microphone=(), geolocation=(), payment=(), usb=()")))
                                // 🧠 FIX 2: Correct placement to ensure CORS headers are sent even on blocked
                                // requests
                                .addFilterBefore(sessionAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .addFilterAfter(rateLimitingFilter, CorsFilter.class);

                return http.build();
        }

        private String buildCspPolicy() {
                // 🧠 FIX 3: Dynamic CSP. We must include our allowed origins in connect-src
                // or the browser will block the frontend from talking to the API.
                List<String> origins = getOriginsList();
                String dynamicConnectSrc = String.join(" ", origins);

                String connectSrc = "'self' " + dynamicConnectSrc + " " + (cspConnectSrc != null ? cspConnectSrc : "");

                return String.join("; ",
                                "default-src 'self'",
                                "script-src 'self' https://accounts.google.com https://apis.google.com",
                                "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://accounts.google.com",
                                "font-src 'self' https://fonts.gstatic.com",
                                "img-src 'self' data: https://images.unsplash.com https://*.amazonaws.com https://lh3.googleusercontent.com",
                                "connect-src " + connectSrc.trim() + " https://accounts.google.com https://oauth2.googleapis.com",
                                "frame-src https://accounts.google.com",
                                "frame-ancestors 'self'",
                                "object-src 'none'",
                                "base-uri 'self'",
                                "form-action 'self'");
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // 🧠 FIX 4: Robust Sanitization. Removes spaces and empty strings from .env
                // input.
                List<String> origins = getOriginsList();
                log.info("CORS: Initializing with allowed origins: {}", origins);

                configuration.setAllowedOrigins(origins);
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                configuration.setAllowedHeaders(List.of(
                                "Content-Type", "Accept", "X-Requested-With", "Idempotency-Key",
                                "Authorization", "Cache-Control", "X-Client-Trace-Id"));

                configuration.setExposedHeaders(List.of("X-Request-Id", "Idempotency-Key"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        private List<String> getOriginsList() {
                return Arrays.stream(allowedOrigins.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }
}