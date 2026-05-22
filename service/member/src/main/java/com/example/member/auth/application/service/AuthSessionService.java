package com.example.member.auth.application.service;

import com.example.member.auth.application.dto.result.AuthSessionListResult;
import com.example.member.auth.application.dto.result.AuthSessionResult;
import com.example.member.auth.application.port.in.AuthSessionUsecase;
import com.example.member.auth.exception.RefreshTokenNotFoundException;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
import com.example.member.auth.infrastructure.redis.auth.ParsedAccessToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.redis.auth.TokenBlacklistStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService implements AuthSessionUsecase {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenBlacklistStore tokenBlacklistStore;

    @Override
    public AuthSessionListResult getSessions(UUID memberId, UUID currentSessionId) {
        List<AuthSessionResult> sessions = refreshTokenStore.findSessionsByMemberId(memberId).stream()
                .sorted(Comparator.comparing(AuthSession::lastAccessedAt).reversed()
                        .thenComparing(AuthSession::createdAt, Comparator.reverseOrder()))
                .map(session -> new AuthSessionResult(
                        session.sessionId(),
                        session.createdAt(),
                        session.lastAccessedAt(),
                        session.lastRefreshedAt(),
                        session.userAgent(),
                        session.ipAddress(),
                        Objects.equals(session.sessionId(), currentSessionId)
                ))
                .toList();
        return new AuthSessionListResult(sessions);
    }

    @Override
    public void logoutSession(
            String accessToken,
            UUID memberId,
            UUID currentSessionId,
            UUID targetSessionId
    ) {
        if (Objects.equals(currentSessionId, targetSessionId)) {
            logoutCurrentSession(accessToken);
            return;
        }

        AuthSession authSession = refreshTokenStore.findBySessionId(targetSessionId)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!Objects.equals(authSession.memberId(), memberId)) {
            throw new RefreshTokenNotFoundException();
        }

        refreshTokenStore.deleteSession(memberId, targetSessionId);
        tokenBlacklistStore.blacklistSession(
                targetSessionId,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );
    }

    @Override
    public void logoutCurrentSession(String accessToken) {
        ParsedAccessToken parsedAccessToken = parseRequiredAccessToken(accessToken);
        refreshTokenStore.deleteSession(parsedAccessToken.memberId(), parsedAccessToken.sessionId());
        tokenBlacklistStore.blacklistAccessToken(
                parsedAccessToken.accessTokenId(),
                remainingTtl(parsedAccessToken.expiresAt())
        );
        tokenBlacklistStore.blacklistSession(
                parsedAccessToken.sessionId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );
    }

    @Override
    public void logoutAllSessions(String accessToken) {
        ParsedAccessToken parsedAccessToken = parseRequiredAccessToken(accessToken);
        Set<UUID> sessionIds = refreshTokenStore.findSessionIdsByMemberId(parsedAccessToken.memberId());
        for (UUID sessionId : sessionIds) {
            tokenBlacklistStore.blacklistSession(
                    sessionId,
                    Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
            );
        }
        refreshTokenStore.deleteAllSessions(parsedAccessToken.memberId());
        tokenBlacklistStore.blacklistAccessToken(
                parsedAccessToken.accessTokenId(),
                remainingTtl(parsedAccessToken.expiresAt())
        );
    }

    @Override
    public void logout(UUID memberId) {
        refreshTokenStore.deleteAllSessions(memberId);
    }

    private ParsedAccessToken parseRequiredAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Authorization 헤더는 필수입니다.");
        }
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return jwtTokenProvider.parseAccessToken(token);
    }

    private Duration remainingTtl(Instant expiresAt) {
        Duration remaining = Duration.between(Instant.now(), expiresAt);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }
}
