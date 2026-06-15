package com.example.member.auth.application.service.session;

import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.member.domain.entity.Member;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthTokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public AuthTokenResult issue(Member member, AuthSessionMetadata metadata) {
        // [1] 세션 ID 생성
        UUID sessionId = UUID.randomUUID();

        // [2] Access Token 발급
        String accessToken = jwtTokenProvider.createAccessToken(member, sessionId);

        // [3] Refresh Token 발급
        String refreshToken = jwtTokenProvider.createRefreshToken(member, sessionId);

        // [4] Refresh Token 파싱
        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);

        // [5] 세션 저장
        refreshTokenStore.createSession(
                member.getMemberId(),
                sessionId,
                parsedRefreshToken.refreshTokenId(),
                Duration.ofMillis(jwtTokenProvider.getRefreshExpiration()),
                metadataOrEmpty(metadata)
        );

        // [6] 토큰 결과 반환
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
