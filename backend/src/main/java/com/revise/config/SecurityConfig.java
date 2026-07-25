package com.revise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
        // Disable CSRF (Cross-Site Request Forgery) since we are building a stateless REST API
        .csrf(csrf -> csrf.disable())

        // Configure route access rules
        .authorizeHttpRequests(auth -> auth
            // Whitelist our authentication and user creation endpoints so anyone can access them
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/user/**").permitAll()

            // Any other request must be authenticated
            .anyRequest().authenticated()
        );

        return http.build();
    }
}
