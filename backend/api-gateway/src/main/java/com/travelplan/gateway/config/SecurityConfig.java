package com.travelplan.gateway.config;

import com.travelplan.gateway.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins:http://localhost:3000,http://*:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
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
                                "/api/vehicles",
                                "/api/vehicles/search",
                                "/api/vehicles/{id}",
                                "/api/vehicles/{id}/availability",
                                "/api/packages",
                                "/api/packages/{id}",
                                "/api/packages/featured",
                                "/api/public/events",
                                "/api/public/events/{id}",
                                "/api/events",
                                "/api/events/{id}",
                                "/api/events/{id}/availability",
                                "/api/products",
                                "/api/products/{id}",
                                "/api/products/images/**",
                                "/api/reviews/entity/**",
                                "/api/reviews/summary/**",
                                "/api/reviews/{id}"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
