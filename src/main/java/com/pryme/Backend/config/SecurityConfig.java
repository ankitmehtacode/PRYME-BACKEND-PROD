package com.pryme.Backend.config;

import com.pryme.Backend.iam.SessionAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    // 🧠 Defaults to include your Vite local port (8081) and Prod domain
    @Value("${app.security.allowed-origins:http://localhost:3000,http://localhost:8081,http://localhost:5173,https://pryme.in}")
    private String allowedOrigins;

    @Value("${app.security.enable-h2-console:false}")
    private boolean enableH2Console;

    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    public SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 🧠 ZERO-TRUST EXCEPTION HANDLING
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required or Token expired\"}");
                }))

                // 🧠 THE GATEKEEPER MATRIX
                .authorizeHttpRequests(auth -> {
                    // 1. Preflight Requests
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // 2. Database Console (If Enabled)
                    if (enableH2Console) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }

                    auth
                            .requestMatchers("/error").permitAll()
                            // 🧠 PRODUCTION FIX 1: Whitelist the ENTIRE auth block (login + register)
                            .requestMatchers("/api/v1/auth/**").permitAll()

                            // 🧠 PRODUCTION FIX 2: Allow anonymous Lead Capture (Frontend hits this before login)
                            .requestMatchers(HttpMethod.POST, "/api/v1/leads").permitAll()

                            // 3. Public Utilities
                            .requestMatchers("/api/v1/public/**", "/api/v1/eligibility/**", "/api/v1/calculators/**").permitAll()

                            // 4. Strict RBAC for Admin Core
                            .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE")
                            .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                            // 5. Lockdown everything else (Dashboard, Document Vault, Elevation)
                            .anyRequest().authenticated();
                })

                // 🧠 DYNAMIC CSP & HEADERS (Preserved from your code)
                .headers(headers -> {
                    String cspDirective = enableH2Console
                            ? "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; frame-ancestors 'self'; object-src 'none'"
                            : "default-src 'self'; frame-ancestors 'self'; object-src 'none'";

                    headers.contentSecurityPolicy(csp -> csp.policyDirectives(cspDirective))
                            .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                            .frameOptions(frame -> frame.sameOrigin()); // Same-origin is required for H2 console frames
                })
                .httpBasic(AbstractHttpConfigurer::disable)

                // Injecting your custom JWT session filter
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

        // 🧠 PRODUCTION FIX 3: Must be TRUE to allow the React frontend to pass the Bearer token headers securely
        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Idempotency-Key"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}