package com.travelplan.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String userId = claims.getSubject();
                String role = extractAppRole(claims);

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, token, authorities);

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

            } catch (Exception e) {
                log.error("JWT validation failed at gateway: {}", e.getMessage());
            }
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") ||
                path.equals("/health") ||
                path.startsWith("/eureka") ||
                path.equals("/api/tourists/register") ||
                path.startsWith("/api/chat") ||
                isPublicBrowsingPath(path);
    }

    private boolean isPublicBrowsingPath(String path) {
        String method = "GET";
        // Hotels public browsing
        if (path.equals("/api/hotels") || path.equals("/api/hotels/search") ||
                path.equals("/api/hotels/query") || path.matches("/api/hotels/\\d+") ||
                path.matches("/api/hotels/\\d+/details") ||
                path.matches("/api/hotels/\\d+/availability")) {
            return true;
        }
        // Rooms public browsing
        if (path.startsWith("/api/rooms")) {
            return true;
        }
        // Guides public browsing
        if (path.equals("/api/tour-guides") || path.equals("/api/tour-guides/search") ||
                path.matches("/api/tour-guides/[^/]+") ||
                path.matches("/api/tour-guides/[^/]+/availability")) {
            return true;
        }
        // Reviews public browsing
        if (path.startsWith("/api/reviews/entity/") ||
                path.startsWith("/api/reviews/summary/") ||
                path.matches("/api/reviews/\\d+")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private String extractAppRole(Claims claims) {
        Map<String, Object> appMetadata = claims.get("app_metadata", Map.class);
        if (appMetadata != null && appMetadata.get("role") != null) {
            return (String) appMetadata.get("role");
        }

        Map<String, Object> userMetadata = claims.get("user_metadata", Map.class);
        if (userMetadata != null && userMetadata.get("role") != null) {
            return (String) userMetadata.get("role");
        }

        return "TOURIST";
    }
}
