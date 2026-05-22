package com.example.member.auth.application.service;

import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.port.in.AuthTokenRefreshUsecase;
import com.example.member.auth.exception.RefreshTokenNotFoundException;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import com.todaylunch.common.security.exception.InvalidTokenException;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthTokenRefreshService implements AuthTokenRefreshUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final LoginEligibilityValidator loginEligibilityValidator;

    @Override
    public AuthTokenResult refresh(TokenRefreshCommand command) {
        validateRefreshCommand(command);

        String refreshToken = normalizeRequired(command.refreshToken(), "refreshToken");
        jwtTokenProvider.validateRefreshToken(refreshToken);
        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);

        loginEligibilityValidator.validateLoginRestriction(parsedRefreshToken.memberId());

        AuthSession authSession = refreshTokenStore.findBySessionId(parsedRefreshToken.sessionId())
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (!Objects.equals(authSession.memberId(), parsedRefreshToken.memberId())
                || !Objects.equals(authSession.refreshTokenId(), parsedRefreshToken.refreshTokenId())) {
            // TODO: refresh token 탈취 감지 시 session blacklist 등록 또는 전체 세션 강제 로그아웃 처리 검토
            refreshTokenStore.deleteSession(parsedRefreshToken.memberId(), parsedRefreshToken.sessionId());
            throw new InvalidTokenException();
        }

        Member member = memberPersistencePort.findById(parsedRefreshToken.memberId())
                .orElseThrow(InvalidTokenException::new);
        loginEligibilityValidator.validateActiveMember(member);

        String newAccessToken = jwtTokenProvider.createAccessToken(member, parsedRefreshToken.sessionId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member, parsedRefreshToken.sessionId());
        ParsedRefreshToken rotatedRefreshToken = jwtTokenProvider.parseRefreshToken(newRefreshToken);
        refreshTokenStore.updateRefreshTokenId(
                parsedRefreshToken.sessionId(),
                rotatedRefreshToken.refreshTokenId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()),
                metadataOrEmpty(command.authSessionMetadata())
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

    private AuthSessionMetadata metadataOrEmpty(AuthSessionMetadata metadata) {
        return metadata == null ? AuthSessionMetadata.empty() : metadata;
    }
}
