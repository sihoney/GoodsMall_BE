package com.example.member.auth.application.service.session;

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
        // [1] 세션 조회
        List<AuthSessionResult> sessions = refreshTokenStore.findSessionsByMemberId(memberId).stream()
                // [2] 최신순 정렬
                .sorted(Comparator.comparing(AuthSession::lastAccessedAt).reversed()
                        .thenComparing(AuthSession::createdAt, Comparator.reverseOrder()))
                // [3] 응답 변환
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

        // [4] 목록 반환
        return new AuthSessionListResult(sessions);
    }

    @Override
    public void logoutSession(
            String accessToken,
            UUID memberId,
            UUID currentSessionId,
            UUID targetSessionId
    ) {
        // [1] 현재 세션 여부 확인
        if (Objects.equals(currentSessionId, targetSessionId)) {
            logoutCurrentSession(accessToken);
            return;
        }

        // [2] 대상 세션 조회
        AuthSession authSession = refreshTokenStore.findBySessionId(targetSessionId)
                .orElseThrow(RefreshTokenNotFoundException::new);

        // [3] 세션 소유자 검증
        if (!Objects.equals(authSession.memberId(), memberId)) {
            throw new RefreshTokenNotFoundException();
        }

        // [4] 세션 삭제
        refreshTokenStore.deleteSession(memberId, targetSessionId);

        // [5] 세션 차단
        // TODO: auth:session:{sessionId} whitelist 검증으로 대체되면 session blacklist 저장을 제거한다.
        tokenBlacklistStore.blacklistSession(
                targetSessionId,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );
    }

    @Override
    public void logoutCurrentSession(String accessToken) {
        // [1] 토큰 파싱
        ParsedAccessToken parsedAccessToken = parseRequiredAccessToken(accessToken);

        // [2] 세션 삭제
        refreshTokenStore.deleteSession(parsedAccessToken.memberId(), parsedAccessToken.sessionId());

        // [3] Access Token 차단
        tokenBlacklistStore.blacklistAccessToken(
                parsedAccessToken.accessTokenId(),
                remainingTtl(parsedAccessToken.expiresAt())
        );

        // [4] 세션 차단
        // TODO: auth:session:{sessionId} whitelist 검증으로 대체되면 session blacklist 저장을 제거한다.
        tokenBlacklistStore.blacklistSession(
                parsedAccessToken.sessionId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
        );
    }

    @Override
    public void logoutAllSessions(String accessToken) {
        // [1] 토큰 파싱
        ParsedAccessToken parsedAccessToken = parseRequiredAccessToken(accessToken);

        // [2] 세션 목록 조회
        Set<UUID> sessionIds = refreshTokenStore.findSessionIdsByMemberId(parsedAccessToken.memberId());

        // [3] 세션 차단
        for (UUID sessionId : sessionIds) {
            // TODO: auth:session:{sessionId} whitelist 검증으로 대체되면 session blacklist 저장을 제거한다.
            tokenBlacklistStore.blacklistSession(
                    sessionId,
                    Duration.ofMillis(jwtTokenProvider.getRefreshExpiration())
            );
        }

        // [4] 전체 세션 삭제
        refreshTokenStore.deleteAllSessions(parsedAccessToken.memberId());

        // [5] Access Token 차단
        tokenBlacklistStore.blacklistAccessToken(
                parsedAccessToken.accessTokenId(),
                remainingTtl(parsedAccessToken.expiresAt())
        );
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
