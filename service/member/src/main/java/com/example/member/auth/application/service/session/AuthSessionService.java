package com.example.member.auth.application.service.session;


import com.example.member.common.exception.BusinessException;
import com.example.member.auth.application.dto.result.AuthSessionListResult;
import com.example.member.auth.application.dto.result.AuthSessionResult;
import com.example.member.auth.application.port.in.AuthSessionUsecase;
import com.example.member.auth.exception.AuthErrorCode;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
import com.example.member.auth.infrastructure.redis.auth.ParsedAccessToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService implements AuthSessionUsecase {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

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
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_TOKEN));

        if (!Objects.equals(authSession.memberId(), memberId)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        refreshTokenStore.deleteSession(memberId, targetSessionId);
    }

    @Override
    public void logoutCurrentSession(String accessToken) {
        ParsedAccessToken parsedAccessToken = parseRequiredAccessToken(accessToken);
        refreshTokenStore.deleteSession(parsedAccessToken.memberId(), parsedAccessToken.sessionId());
    }

    @Override
    public void logoutAllSessions(String accessToken) {
        ParsedAccessToken parsedAccessToken = parseRequiredAccessToken(accessToken);
        refreshTokenStore.deleteAllSessions(parsedAccessToken.memberId());
    }

    private ParsedAccessToken parseRequiredAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }
        String token = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        return jwtTokenProvider.parseAccessToken(token);
    }
}
