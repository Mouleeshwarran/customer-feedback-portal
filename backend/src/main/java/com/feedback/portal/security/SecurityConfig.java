
package com.feedback.portal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/h2-console/**", "/api/**"
            ))
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(
                    HttpMethod.GET, "/api/feedback/public"
                ).permitAll()
                .requestMatchers("/api/admin/**")
                    .hasAnyRole("MODERATOR", "ADMINISTRATOR")
                .requestMatchers("/api/feedback/my")
                    .hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/feedback")
                    .hasRole("CUSTOMER")
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {})
            .logout(logout -> logout.permitAll());

        return http.build();
    }
}
