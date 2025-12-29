package kr.ac.koreatech.sw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure authorization rules
            .authorizeHttpRequests(authorize -> authorize
                // Allow public access to health check and actuator endpoints
                .requestMatchers("/health", "/actuator/health").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            // Enable CSRF protection (default, but explicit for clarity)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/actuator/**")
            )
            // Configure security headers
            .headers(headers -> headers
                // Prevent clickjacking
                .frameOptions(frame -> frame.deny())
                // Prevent MIME type sniffing
                .contentTypeOptions(content -> {})
                // Enable XSS protection
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                )
                // HTTP Strict Transport Security (HSTS)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        return http.build();
    }
}
