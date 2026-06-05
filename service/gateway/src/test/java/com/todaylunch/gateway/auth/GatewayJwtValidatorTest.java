package com.todaylunch.gateway.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GatewayJwtValidatorTest {

    private static final String SECRET = "12345678901234567890123456789012";
    private static final String ISSUER = "member-service";

    @Mock
    private SessionWhitelistStore sessionWhitelistStore;

    private GatewayJwtValidator gatewayJwtValidator;

    @BeforeEach
    void setUp() {
        gatewayJwtValidator = new GatewayJwtValidator(
                new JwtProperties(SECRET, ISSUER),
                sessionWhitelistStore
        );
    }

    @Test
    void validateAccessToken_withWhitelistedSession_returnsPrincipal() {
        UUID memberId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = createToken("ACCESS", memberId, sessionId, "USER");

        when(sessionWhitelistStore.hasSession(memberId, sessionId)).thenReturn(true);

        AuthenticatedPrincipal principal = gatewayJwtValidator.validateAccessToken(token);

        assertEquals(memberId, principal.memberId());
        assertEquals(sessionId, principal.sessionId());
        assertEquals("USER", principal.role());
        verify(sessionWhitelistStore).hasSession(memberId, sessionId);
    }

    @Test
    void validateAccessToken_withoutWhitelistedSession_throwsInvalidToken() {
        UUID memberId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = createToken("ACCESS", memberId, sessionId, "USER");

        when(sessionWhitelistStore.hasSession(memberId, sessionId)).thenReturn(false);

        AuthException exception = assertThrows(
                AuthException.class,
                () -> gatewayJwtValidator.validateAccessToken(token)
        );

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    @Test
    void validateAccessToken_withRefreshToken_throwsInvalidToken() {
        UUID memberId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        String token = createToken("REFRESH", memberId, sessionId, "USER");

        AuthException exception = assertThrows(
                AuthException.class,
                () -> gatewayJwtValidator.validateAccessToken(token)
        );

        assertEquals(AuthErrorCode.INVALID_TOKEN, exception.getErrorCode());
    }

    private String createToken(String tokenType, UUID memberId, UUID sessionId, String role) {
        Instant now = Instant.now();
        SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .issuer(ISSUER)
                .subject(memberId.toString())
                .id(UUID.randomUUID().toString())
                .claim("memberId", memberId.toString())
                .claim("sessionId", sessionId.toString())
                .claim("role", role)
                .claim("tokenType", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60)))
                .signWith(secretKey)
                .compact();
    }
}

