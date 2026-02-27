package com.travelplan.gateway.config;

import com.travelplan.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/actuator/**",
                                "/eureka/**",
                                "/api/chat/**"
                        ).permitAll()
                        // Public auth endpoints
                        .pathMatchers("/api/tourists/register").permitAll()
                        // Public browsing endpoints (GET only)
                        .pathMatchers(HttpMethod.GET,
                                "/api/hotels",
                                "/api/hotels/search",
                                "/api/hotels/query",
                                "/api/hotels/{id}",
                                "/api/hotels/{id}/details",
                                "/api/hotels/{id}/availability",
                                "/api/rooms/**",
                                "/api/tour-guides",
                                "/api/tour-guides/search",
                                "/api/tour-guides/{id}",
                                "/api/tour-guides/{id}/availability",
                                "/api/reviews/entity/**",
                                "/api/reviews/summary/**",
                                "/api/reviews/{id}"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
