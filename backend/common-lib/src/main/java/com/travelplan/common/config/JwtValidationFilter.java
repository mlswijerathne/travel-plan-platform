package com.travelplan.common.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    private volatile PublicKey jwksPublicKey;
    private volatile long jwksLastFetched;
    private static final long JWKS_CACHE_DURATION_MS = 3600_000; // 1 hour

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(supabaseUrl)) {
            try {
                fetchJwksPublicKey();
            } catch (Exception e) {
                log.warn("Failed to fetch JWKS on startup, will retry on first request: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = parseToken(token);

                String userId = claims.getSubject();
                String role = extractAppRole(claims);

                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, token, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private Claims parseToken(String token) {
        // Try ES256 (JWKS) first if supabase URL is configured
        if (StringUtils.hasText(supabaseUrl)) {
            try {
                PublicKey publicKey = getJwksPublicKey();
                if (publicKey != null) {
                    return Jwts.parser()
                            .verifyWith(publicKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
                }
            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.debug("ES256 verification failed, falling back to HS256: {}", e.getMessage());
            } catch (Exception e) {
                log.debug("ES256 verification error, falling back to HS256: {}", e.getMessage());
            }
        }

        // Fallback to HS256 (legacy)
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private PublicKey getJwksPublicKey() {
        if (jwksPublicKey == null || System.currentTimeMillis() - jwksLastFetched > JWKS_CACHE_DURATION_MS) {
            synchronized (this) {
                if (jwksPublicKey == null || System.currentTimeMillis() - jwksLastFetched > JWKS_CACHE_DURATION_MS) {
                    fetchJwksPublicKey();
                }
            }
        }
        return jwksPublicKey;
    }

    @SuppressWarnings("unchecked")
    private void fetchJwksPublicKey() {
        try {
            String jwksUrl = supabaseUrl.replaceAll("/$", "") + "/auth/v1/.well-known/jwks.json";
            log.info("Fetching JWKS from: {}", jwksUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("JWKS fetch failed with status: {}", response.statusCode());
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jwks = mapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

            if (keys == null || keys.isEmpty()) {
                log.error("No keys found in JWKS response");
                return;
            }

            // Find the ES256 signing key
            for (Map<String, Object> key : keys) {
                String alg = (String) key.get("alg");
                String kty = (String) key.get("kty");
                if ("ES256".equals(alg) && "EC".equals(kty)) {
                    String x = (String) key.get("x");
                    String y = (String) key.get("y");
                    jwksPublicKey = buildEcPublicKey(x, y);
                    jwksLastFetched = System.currentTimeMillis();
                    log.info("JWKS ES256 public key loaded successfully (kid: {})", key.get("kid"));
                    return;
                }
            }

            log.warn("No ES256 key found in JWKS, will use HS256 fallback");
        } catch (Exception e) {
            log.error("Failed to fetch/parse JWKS: {}", e.getMessage());
        }
    }

    private PublicKey buildEcPublicKey(String xBase64Url, String yBase64Url) throws Exception {
        byte[] xBytes = Base64.getUrlDecoder().decode(xBase64Url);
        byte[] yBytes = Base64.getUrlDecoder().decode(yBase64Url);

        BigInteger x = new BigInteger(1, xBytes);
        BigInteger y = new BigInteger(1, yBytes);

        AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
        params.init(new ECGenParameterSpec("secp256r1")); // P-256
        ECParameterSpec ecSpec = params.getParameterSpec(ECParameterSpec.class);

        ECPoint point = new ECPoint(x, y);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, ecSpec);

        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(pubSpec);
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator") ||
                path.equals("/health") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui");
    }
}
