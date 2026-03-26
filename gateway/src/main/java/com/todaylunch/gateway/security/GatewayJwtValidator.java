package com.todaylunch.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class GatewayJwtValidator {

    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS = "ACCESS";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public GatewayJwtValidator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            throw new IllegalStateException("JWT secret is not configured for gateway-service.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthenticatedPrincipal validateAccessToken(String token) {
        // JWT 서명/만료/claim 검증
        Claims claims = Jwts.parser()
                .requireIssuer(jwtProperties.issuer())
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // todo: blacklist 검증

        if (!ACCESS.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new IllegalArgumentException("Only access tokens are accepted.");
        }

        return new AuthenticatedPrincipal(
                UUID.fromString(claims.get(MEMBER_ID_CLAIM, String.class)),
                claims.get(ROLE_CLAIM, String.class)
        );
    }

    public record AuthenticatedPrincipal(UUID memberId, String role) {
    }
}
