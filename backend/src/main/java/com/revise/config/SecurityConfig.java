package com.revise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.revise.security.JwtAuthenticationEntryPoint;
import com.revise.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Inject the Jwt filter
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Inject the Jwt entry point
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
        // Disable CSRF (Cross-Site Request Forgery) since we are building a stateless REST API
        .csrf(csrf -> csrf.disable())

        // Add exception handling to route unauthenticated errors to our custom entry point

        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        )

        // Set session management to stateless (no server-side cookies)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure route access rules
        .authorizeHttpRequests(auth -> auth
            // Whitelist our authentication and user creation endpoints so anyone can access them
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/user/**").permitAll()

            // Any other request must be authenticated
            .anyRequest().authenticated()
        );

        // Inject the JWT filter Before the standard UsernamePassword filter 
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
