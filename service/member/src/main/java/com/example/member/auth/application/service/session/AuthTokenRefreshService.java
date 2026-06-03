package com.example.member.auth.application.service.session;

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
        // [1] 요청 검증
        validateRefreshCommand(command);

        // [2] 토큰 정규화
        String refreshToken = normalizeRequired(command.refreshToken(), "refreshToken");

        // [3] 토큰 검증
        jwtTokenProvider.validateRefreshToken(refreshToken);

        // [4] 토큰 파싱
        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);

        // [5] 로그인 제한 검증
        loginEligibilityValidator.validateLoginRestriction(parsedRefreshToken.memberId());

        // [6] 세션 조회
        AuthSession authSession = refreshTokenStore.findBySessionId(parsedRefreshToken.sessionId())
                .orElseThrow(RefreshTokenNotFoundException::new);

        // [7] 세션 일치 검증
        if (!Objects.equals(authSession.memberId(), parsedRefreshToken.memberId())
                || !Objects.equals(authSession.refreshTokenId(), parsedRefreshToken.refreshTokenId())) {
            // TODO: refresh token 탈취 감지 시 session blacklist 등록 또는 전체 세션 강제 로그아웃 처리 검토
            refreshTokenStore.deleteSession(parsedRefreshToken.memberId(), parsedRefreshToken.sessionId());
            throw new InvalidTokenException();
        }

        // [8] 회원 조회
        Member member = memberPersistencePort.findById(parsedRefreshToken.memberId())
                .orElseThrow(InvalidTokenException::new);

        // [9] 회원 상태 검증
        loginEligibilityValidator.validateActiveMember(member);

        // [10] Access Token 재발급
        String newAccessToken = jwtTokenProvider.createAccessToken(member, parsedRefreshToken.sessionId());

        // [11] Refresh Token 재발급
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member, parsedRefreshToken.sessionId());

        // [12] Refresh Token 파싱
        ParsedRefreshToken rotatedRefreshToken = jwtTokenProvider.parseRefreshToken(newRefreshToken);

        // [13] 세션 갱신
        refreshTokenStore.updateRefreshTokenId(
                parsedRefreshToken.sessionId(),
                rotatedRefreshToken.refreshTokenId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()),
                metadataOrEmpty(command.authSessionMetadata())
        );

        // [14] 토큰 결과 반환
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
