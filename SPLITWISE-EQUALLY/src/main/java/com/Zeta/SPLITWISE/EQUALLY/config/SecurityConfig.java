package com.Zeta.SPLITWISE.EQUALLY.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Enables Spring Security features (like PasswordEncoder)
public class SecurityConfig {

    /**
     * Defines the standard BCryptPasswordEncoder bean for robust password hashing.
     * This bean is then injected into the UserService.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Disables Spring Security's default filters.
     * We allow all requests because this application handles authentication via its own REST API endpoints.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable Cross-Site Request Forgery protection (common for stateless APIs)
                .csrf(csrf -> csrf.disable())

                // Allow requests from any origin/path to proceed without authentication
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}