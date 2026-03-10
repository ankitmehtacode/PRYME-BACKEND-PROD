package com.pryme.Backend.config;


import com.pryme.Backend.iam.SessionAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
main
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
 
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

main
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
main
import java.util.List;

@Configuration
public class SecurityConfig {

 codex/create-45-day-execution-plan-0e2u1d
    @Value("${app.security.allowed-origins:http://localhost:3000,https://pryme.in}")
    private String allowedOrigins;

    @Value("${app.security.enable-h2-console:false}")
    private boolean enableH2Console;

    private final SessionAuthenticationFilter sessionAuthenticationFilter;

    public SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
    }


 main
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
 codex/create-45-day-execution-plan-0e2u1d
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}");
                }))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    if (enableH2Console) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }
                    auth
                            .requestMatchers("/error").permitAll()
                            .requestMatchers("/api/v1/auth/login").permitAll()
                            .requestMatchers("/api/v1/public/**", "/api/v1/eligibility/**", "/api/v1/calculators/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE")
                            .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                            .anyRequest().authenticated();
                })

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/public/**", "/api/v1/eligibility/**", "/api/v1/calculators/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated()
                )
 main
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; frame-ancestors 'self'; object-src 'none'"))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                        .frameOptions(frame -> frame.sameOrigin())
                )
 codex/create-45-day-execution-plan-0e2u1d
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
=======
                .httpBasic(Customizer.withDefaults());
 main

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
codex/create-45-day-execution-plan-0e2u1d
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Idempotency-Key"));

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://pryme.in"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
main
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
