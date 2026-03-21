package com.pryme.Backend.config;

import com.pryme.Backend.iam.RateLimitingFilter;
import com.pryme.Backend.iam.SessionAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
// 🧠 CRITICAL FIX: Without this, your @PreAuthorize tags in AdminController are completely ignored!
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.allowed-origins:http://localhost:3000,http://localhost:8081,http://localhost:5173,https://pryme.in}")
    private String allowedOrigins;

    @Value("${app.security.enable-h2-console:false}")
    private boolean enableH2Console;

    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;

    // 🧠 Dependency Injection: Now injecting both the Auth Filter and the Rate Limiter Shield
    public SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter, RateLimitingFilter rateLimitingFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST APIs
                .csrf(AbstractHttpConfigurer::disable)

                // EXPLICIT CORS: Forces Spring to use your custom bean below, preventing pre-flight drops
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // STATELESS SESSIONS: Guarantees no JSESSIONID memory leaks in Tomcat
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ZERO-TRUST EXCEPTION HANDLING
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication matrix required or Token expired.\"}");
                }))

                // 🧠 THE GATEKEEPER MATRIX
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    if (enableH2Console) {
                        auth.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll();
                    }

                    // Whitelisted Public Vectors
                    auth.requestMatchers(new AntPathRequestMatcher("/error")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**")).permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/leads").permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/api/v1/public/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/api/v1/eligibility/**")).permitAll()
                            .requestMatchers(new AntPathRequestMatcher("/api/v1/calculators/**")).permitAll();

                    // Baseline Admin Protection (Method-level @PreAuthorize provides the primary shield)
                    auth.requestMatchers(new AntPathRequestMatcher("/api/v1/admin/**")).hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE");

                    // Lockdown everything else
                    auth.anyRequest().authenticated();
                })

                // DYNAMIC CSP & SECURE HEADERS
                .headers(headers -> {
                    String cspDirective = enableH2Console
                            ? "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'; object-src 'none'"
                            : "default-src 'self'; frame-ancestors 'self'; object-src 'none'";

                    headers.contentSecurityPolicy(csp -> csp.policyDirectives(cspDirective))
                            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                            .frameOptions(frame -> frame.sameOrigin());
                })

                // 🧠 THE TITANIUM FILTER CHAIN ORDER
                // 1. Rate Limiting Filter drops malicious botnet requests instantly using Caffeine RAM Cache.
                // 2. Auth Filter parses JWTs only for legitimate, non-spam traffic.
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(sessionAuthenticationFilter, RateLimitingFilter.class);

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
        // Added "Accept" to allowed headers to prevent edge-case CORS drops
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Idempotency-Key", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 🧠 Financial Grade Upgrade: Increased rounds from default 10 to 12 for modern brute-force resistance.
        return new BCryptPasswordEncoder(12);
    }
}