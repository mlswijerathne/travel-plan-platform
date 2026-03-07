package com.travelplan.booking.config;

import com.travelplan.common.config.JwtValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class BookingSecurityConfig {

    private final JwtValidationFilter jwtValidationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/**",
                    "/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/public/**")
                .permitAll()
                // Internal service-to-service endpoints (no auth required)
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/itinerary/*").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/items/*/status").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/bookings/availability-check").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bookings/provider/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/bookings/reference/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
