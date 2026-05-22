package com.todaylunch.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class GatewayJwtValidator {

    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS = "ACCESS";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final TokenBlacklistStore tokenBlacklistStore;

    public GatewayJwtValidator(JwtProperties jwtProperties, TokenBlacklistStore tokenBlacklistStore) {
        this.jwtProperties = jwtProperties;
        this.tokenBlacklistStore = tokenBlacklistStore;
        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            throw new IllegalStateException("JWT secret is not configured for gateway-service.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthenticatedPrincipal validateAccessToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .requireIssuer(jwtProperties.issuer())
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new AuthException(AuthErrorCode.TOKEN_EXPIRED, exception);
        } catch (Exception exception) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN, exception);
        }

        if (!ACCESS.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        String accessTokenId = claims.getId();
        String sessionId = claims.get(SESSION_ID_CLAIM, String.class);
        if (accessTokenId == null || accessTokenId.isBlank() || sessionId == null || sessionId.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        // TODO: session blacklist 삭제 시 auth:session:{sessionId} whitelist 검증 방식으로 전환 검토
        if (tokenBlacklistStore.isAccessTokenBlacklisted(accessTokenId)
                || tokenBlacklistStore.isSessionBlacklisted(UUID.fromString(sessionId))) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return new AuthenticatedPrincipal(
                UUID.fromString(claims.get(MEMBER_ID_CLAIM, String.class)),
                claims.get(ROLE_CLAIM, String.class),
                UUID.fromString(sessionId)
        );
    }

    public record AuthenticatedPrincipal(UUID memberId, String role, UUID sessionId) {
    }
}
