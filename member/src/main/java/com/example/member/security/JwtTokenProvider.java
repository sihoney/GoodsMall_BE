package com.example.member.security;

import com.example.member.domain.entity.Member;
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
            throw new IllegalStateException("JWT secret is not configured for member-service.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 엑세스 토큰 생성
    public String createAccessToken(Member member) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(member.getMemberId().toString())
                .claim(MEMBER_ID_CLAIM, member.getMemberId().toString())
                .claim(EMAIL_CLAIM, member.getEmail())
                .claim(ROLE_CLAIM, member.getRole().name())
                .claim(TOKEN_TYPE_CLAIM, ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.accessExpiration())))
                .signWith(secretKey)
                .compact();
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(Member member) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(jwtProperties.issuer())
                .subject(member.getMemberId().toString())
                .claim(MEMBER_ID_CLAIM, member.getMemberId().toString())
                .claim(TOKEN_TYPE_CLAIM, REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(jwtProperties.refreshExpiration())))
                .signWith(secretKey)
                .compact();
    }

    public UUID extractMemberId(String token) {
        return UUID.fromString(parseClaims(token).get(MEMBER_ID_CLAIM, String.class));
    }

    public void validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        if (!REFRESH.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
            throw new InvalidTokenException();
        }
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
