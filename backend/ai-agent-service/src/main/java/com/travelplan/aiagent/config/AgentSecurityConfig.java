package com.travelplan.aiagent.config;

import com.travelplan.common.config.JwtValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * AI Agent security config — higher priority than CommonSecurityConfig.
 * Permits /error (needed for SSE streams: Tomcat dispatches to /error after
 * the response is committed, and blocking it corrupts chunked encoding).
 */
@Configuration
@RequiredArgsConstructor
public class AgentSecurityConfig {

    private final JwtValidationFilter jwtValidationFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain agentSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/chat/**", "/error")
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/chat/**").authenticated())
                .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
