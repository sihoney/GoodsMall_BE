package com.example.member.security;

import com.example.member.domain.entity.Member;
import com.example.member.infrastructure.redis.ParsedAccessToken;
import com.example.member.infrastructure.redis.ParsedRefreshToken;
import com.todaylunch.common.security.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String SESSION_ID_CLAIM = "sessionId";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";
    private static final String ACCESS = "ACCESS";
    private static final String REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank() || secret.contains("${")) {
            throw new IllegalStateException("member-service의 JWT secret이 설정되지 않았습니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 엑세스 토큰 생성
    public String createAccessToken(Member member, UUID sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(member.getMemberId().toString())
                .id(UUID.randomUUID().toString())
                .claim(MEMBER_ID_CLAIM, member.getMemberId().toString())
                .claim(SESSION_ID_CLAIM, sessionId.toString())
                .claim(EMAIL_CLAIM, member.getEmail())
                .claim(ROLE_CLAIM, member.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.accessExpiration())))
                .signWith(secretKey)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(Member member, UUID sessionId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(member.getMemberId().toString())
                .id(UUID.randomUUID().toString())
                .claim(MEMBER_ID_CLAIM, member.getMemberId().toString())
                .claim(SESSION_ID_CLAIM, sessionId.toString())
                .claim(TOKEN_TYPE_CLAIM, REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.refreshExpiration())))
                .signWith(secretKey)
                .compact();
    }

    public UUID extractMemberId(String token) {
        return UUID.fromString(parseClaims(token).get(MEMBER_ID_CLAIM, String.class));
    }

    public UUID extractSessionId(String token) {
        return UUID.fromString(parseClaims(token).get(SESSION_ID_CLAIM, String.class));
    }

    public String extractTokenId(String token) {
        String tokenId = parseClaims(token).getId();
        if (tokenId == null || tokenId.isBlank()) {
            throw new InvalidTokenException();
        }
        return tokenId;
    }

    public ParsedAccessToken parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        if (!ACCESS.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new InvalidTokenException();
        }

        String memberId = claims.get(MEMBER_ID_CLAIM, String.class);
        String sessionId = claims.get(SESSION_ID_CLAIM, String.class);
        String tokenId = claims.getId();
        Date expiration = claims.getExpiration();
        if (memberId == null || sessionId == null || tokenId == null || tokenId.isBlank() || expiration == null) {
            throw new InvalidTokenException();
        }

        return new ParsedAccessToken(
                UUID.fromString(memberId),
                UUID.fromString(sessionId),
                tokenId,
                expiration.toInstant()
        );
    }

    public ParsedRefreshToken parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new InvalidTokenException();
        }

        String memberId = claims.get(MEMBER_ID_CLAIM, String.class);
        String sessionId = claims.get(SESSION_ID_CLAIM, String.class);
        String tokenId = claims.getId();
        if (memberId == null || sessionId == null || tokenId == null || tokenId.isBlank()) {
            throw new InvalidTokenException();
        }

        return new ParsedRefreshToken(
                UUID.fromString(memberId),
                UUID.fromString(sessionId),
                tokenId
        );
    }

    public void validateRefreshToken(String token) {
        parseRefreshToken(token);
    }

    public long getAccessExpiration() {
        return jwtProperties.accessExpiration();
    }

    public long getRefreshExpiration() {
        return jwtProperties.refreshExpiration();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception exception) {
            throw new InvalidTokenException();
        }
    }
}
