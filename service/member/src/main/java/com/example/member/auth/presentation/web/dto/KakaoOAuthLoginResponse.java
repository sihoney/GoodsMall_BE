package com.example.member.auth.presentation.web.dto;

import java.util.UUID;

public record KakaoOAuthLoginResponse(
        boolean linkRequired,
        String linkToken,
        String provider,
        String providerUserId,
        String email,
        String nickname,
        String accessToken,
        String refreshToken,
        UUID sessionId,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static KakaoOAuthLoginResponse linked(
            String provider,
            String providerUserId,
            String email,
            String nickname,
            String accessToken,
            String refreshToken,
            UUID sessionId,
            String tokenType,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new KakaoOAuthLoginResponse(
                false,
                null,
                provider,
                providerUserId,
                email,
                nickname,
                accessToken,
                refreshToken,
                sessionId,
                tokenType,
                accessTokenExpiresIn,
                refreshTokenExpiresIn
        );
    }

    public static KakaoOAuthLoginResponse linkRequired(
            String linkToken,
            String provider,
            String providerUserId,
            String email,
            String nickname
    ) {
        return new KakaoOAuthLoginResponse(
                true,
                linkToken,
                provider,
                providerUserId,
                email,
                nickname,
                null,
                null,
                null,
                null,
                0L,
                0L
        );
    }
}

