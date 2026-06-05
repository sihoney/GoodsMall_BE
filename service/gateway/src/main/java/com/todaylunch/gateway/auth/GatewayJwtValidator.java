package com.todaylunch.gateway.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class GatewayJwtValidator {

    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String ROLE_CLAIM = "role";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS = "ACCESS";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final SessionWhitelistStore sessionWhitelistStore;

    public GatewayJwtValidator(JwtProperties jwtProperties, SessionWhitelistStore sessionWhitelistStore) {
        this.jwtProperties = jwtProperties;
        this.sessionWhitelistStore = sessionWhitelistStore;
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
        String memberId = claims.get(MEMBER_ID_CLAIM, String.class);
        String sessionId = claims.get(SESSION_ID_CLAIM, String.class);
        String role = claims.get(ROLE_CLAIM, String.class);
        if (accessTokenId == null || accessTokenId.isBlank()
                || memberId == null || memberId.isBlank()
                || sessionId == null || sessionId.isBlank()
                || role == null || role.isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        UUID parsedMemberId;
        UUID parsedSessionId;
        try {
            parsedMemberId = UUID.fromString(memberId);
            parsedSessionId = UUID.fromString(sessionId);
        } catch (IllegalArgumentException exception) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN, exception);
        }

        if (!sessionWhitelistStore.hasSession(parsedMemberId, parsedSessionId)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return new AuthenticatedPrincipal(
                parsedMemberId,
                role,
                parsedSessionId
        );
    }

}

