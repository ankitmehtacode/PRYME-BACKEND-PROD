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

    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                // EXPLICIT CORS: Prevents React pre-flight drops
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
                    auth.requestMatchers("/error").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/leads").permitAll()
                            .requestMatchers("/api/v1/public/**").permitAll()
                            .requestMatchers("/api/v1/eligibility/**").permitAll()
                            .requestMatchers("/api/v1/calculators/**").permitAll()
                            .requestMatchers("/actuator/health").permitAll();

                    // Baseline Admin Protection
                    auth.requestMatchers("/actuator/**").denyAll()
                            .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE");

                    // Lockdown everything else
                    auth.anyRequest().authenticated();
                })

                // 🧠 DYNAMIC CSP & SECURE HEADERS (H2 Ghost completely purged)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'self'; object-src 'none'"))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // 🧠 THE TITANIUM FILTER CHAIN ORDER
                // 1. Rate Limiting runs AFTER CorsFilter. If a bot gets rate-limited, the browser still gets
                //    the CORS headers, allowing Axios to cleanly read the 429 status instead of throwing a CORS error!
                .addFilterAfter(rateLimitingFilter, CorsFilter.class)
                // 2. Rate Limiting for Eligibility Endpoints
                .addFilterBefore(new RateLimitingFilter("/api/v1/public/eligibility/**", 100, 60), rateLimitingFilter.getClass())
                // 3. Auth filter runs directly before the standard Spring UsernamePassword filter.
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

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
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Idempotency-Key", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));

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
