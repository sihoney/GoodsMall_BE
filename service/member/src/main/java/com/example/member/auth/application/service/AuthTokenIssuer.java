package com.example.member.auth.application.service;

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
