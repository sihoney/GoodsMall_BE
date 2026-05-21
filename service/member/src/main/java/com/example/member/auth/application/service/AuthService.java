package com.example.member.auth.application.service;

import com.example.member.restriction.application.service.MemberRestrictionService;

import com.example.member.auth.application.dto.command.AuthSessionMetadata;
import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthSessionListResult;
import com.example.member.auth.application.dto.result.AuthSessionResult;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.port.in.AuthUsecase;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.verification.exception.EmailVerificationRequiredException;
import com.example.member.auth.exception.InvalidLoginException;
import com.example.member.restriction.exception.MemberRestrictedException;
import com.example.member.member.exception.MemberWithdrawnException;
import com.example.member.auth.exception.RefreshTokenNotFoundException;
import com.example.member.member.domain.entity.Member;
import com.example.member.restriction.domain.entity.MemberRestriction;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
import com.example.member.auth.infrastructure.redis.auth.ParsedAccessToken;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.redis.auth.TokenBlacklistStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import com.todaylunch.common.security.exception.InvalidTokenException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final TokenBlacklistStore tokenBlacklistStore;
    private final MemberRestrictionService memberRestrictionService;

    @Override
    public AuthTokenResult login(LoginCommand command, AuthSessionMetadata metadata) {
        validateLoginCommand(command);

        String email = normalizeRequired(command.email(), "email");
        Member member = memberPersistencePort.findByEmail(email)
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(normalizeRequired(command.password(), "password"), member.getPassword())) {
            throw new InvalidLoginException();
        }

        validateActiveMember(member);
        validateLoginRestriction(member);

        return issueLoginResponse(member, metadata);
    }

    public AuthTokenResult login(Member member, AuthSessionMetadata metadata) {
        validateActiveMember(member);
        validateLoginRestriction(member);
        return issueLoginResponse(member, metadata);
    }

    @Override
    public AuthTokenResult refresh(TokenRefreshCommand command, AuthSessionMetadata metadata) {
        validateRefreshCommand(command);

        String refreshToken = normalizeRequired(command.refreshToken(), "refreshToken");
        jwtTokenProvider.validateRefreshToken(refreshToken);
        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);

        MemberRestriction memberRestriction = memberRestrictionService.getActiveLoginRestriction(
                parsedRefreshToken.memberId(),
                LocalDateTime.now()
        );
        if (memberRestriction != null) {
            throw new MemberRestrictedException(memberRestriction.getEndAt());
        }

        AuthSession authSession = refreshTokenStore.findBySessionId(parsedRefreshToken.sessionId())
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!Objects.equals(authSession.memberId(), parsedRefreshToken.memberId())
                || !Objects.equals(authSession.refreshTokenId(), parsedRefreshToken.refreshTokenId())) {
            refreshTokenStore.deleteSession(parsedRefreshToken.memberId(), parsedRefreshToken.sessionId());
            throw new InvalidTokenException();
        }

        Member member = memberPersistencePort.findById(parsedRefreshToken.memberId())
                .orElseThrow(InvalidTokenException::new);
        validateActiveMember(member);

        String newAccessToken = jwtTokenProvider.createAccessToken(member, parsedRefreshToken.sessionId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member, parsedRefreshToken.sessionId());
        ParsedRefreshToken rotatedRefreshToken = jwtTokenProvider.parseRefreshToken(newRefreshToken);
        refreshTokenStore.updateRefreshTokenId(
                parsedRefreshToken.sessionId(),
                rotatedRefreshToken.refreshTokenId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()),
                metadataOrEmpty(metadata)
        );

        return new AuthTokenResult(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpiration(),
                jwtTokenProvider.getRefreshExpiration(),
                parsedRefreshToken.sessionId()
        );
    }

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

    private void validateLoginCommand(LoginCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("로그인 요청 본문은 필수입니다.");
        }
    }

    private void validateRefreshCommand(TokenRefreshCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("토큰 재발급 요청 본문은 필수입니다.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private void validateLoginRestriction(Member member) {
        MemberRestriction memberRestriction = memberRestrictionService.getActiveLoginRestriction(
                member.getMemberId(),
                LocalDateTime.now()
        );
        if (memberRestriction != null) {
            throw new MemberRestrictedException(memberRestriction.getEndAt());
        }
    }

    private void validateActiveMember(Member member) {
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new MemberWithdrawnException();
        }

        if (!member.isActive()) {
            throw new EmailVerificationRequiredException();
        }
    }

    private AuthTokenResult issueLoginResponse(Member member, AuthSessionMetadata metadata) {
        UUID sessionId = UUID.randomUUID();
        String accessToken = jwtTokenProvider.createAccessToken(member, sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(member, sessionId);
        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);

        refreshTokenStore.createSession(
                member.getMemberId(),
                sessionId,
                parsedRefreshToken.refreshTokenId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()),
                metadataOrEmpty(metadata)
        );

        return new AuthTokenResult(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpiration(),
                jwtTokenProvider.getRefreshExpiration(),
                sessionId
        );
    }

    private AuthSessionMetadata metadataOrEmpty(AuthSessionMetadata metadata) {
        return metadata == null ? AuthSessionMetadata.empty() : metadata;
    }
}

