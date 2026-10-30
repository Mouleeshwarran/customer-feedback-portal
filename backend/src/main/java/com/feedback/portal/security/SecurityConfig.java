
package com.feedback.portal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // CORS — configured via CorsConfig bean
            .cors(cors -> {})

            // CSRF: disabled for REST API with HTTP Basic auth
            // (stateless: no session cookies to forge)
            .csrf(csrf -> csrf.disable())

            // Security headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())       // H2 console
                .referrerPolicy(ref -> ref.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .contentTypeOptions(ct -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000))
            )

            .authorizeHttpRequests(auth -> auth
                // H2 console (dev only)
                .requestMatchers("/h2-console/**").permitAll()

                // OpenAPI / Swagger (dev only)
                .requestMatchers("/api-docs/**", "/swagger-ui/**",
                    "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Public endpoints
                .requestMatchers(HttpMethod.GET, "/api/feedback/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()

                // Customer-only endpoints
                .requestMatchers(HttpMethod.POST, "/api/feedback").hasRole("CUSTOMER")
                .requestMatchers("/api/feedback/my").hasRole("CUSTOMER")

                // Abuse flag — any authenticated user
                .requestMatchers(HttpMethod.POST, "/api/feedback/*/flags")
                    .authenticated()

                // Attachment upload/download — authenticated
                .requestMatchers("/api/feedback/*/attachments/**").authenticated()

                // Admin / Moderator shared endpoints
                .requestMatchers("/api/admin/feedback/**")
                    .hasAnyRole("MODERATOR", "ADMINISTRATOR")
                .requestMatchers("/api/admin/reports/**")
                    .hasAnyRole("MODERATOR", "ADMINISTRATOR")
                .requestMatchers("/api/admin/categories/**")
                    .hasAnyRole("MODERATOR", "ADMINISTRATOR")
                .requestMatchers("/api/admin/stats")
                    .hasAnyRole("MODERATOR", "ADMINISTRATOR")
                .requestMatchers("/api/admin/audit/**")
                    .hasAnyRole("MODERATOR", "ADMINISTRATOR")

                // User management — ADMINISTRATOR only
                .requestMatchers("/api/admin/users/**")
                    .hasRole("ADMINISTRATOR")

                // Dashboard stats — authenticated
                .requestMatchers("/api/feedback/stats").authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // HTTP Basic authentication (suitable for this academic project)
            .httpBasic(basic -> {})

            // Stateless session
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))

            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
