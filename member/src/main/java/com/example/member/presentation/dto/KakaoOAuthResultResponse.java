package com.example.member.presentation.dto;

import java.util.UUID;

public record KakaoOAuthResultResponse(
        KakaoOAuthResultStatus status,
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
        long refreshTokenExpiresIn,
        String errorCode,
        String errorMessage
) {
    public static KakaoOAuthResultResponse success(
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
        return new KakaoOAuthResultResponse(
                KakaoOAuthResultStatus.SUCCESS,
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
                refreshTokenExpiresIn,
                null,
                null
        );
    }

    public static KakaoOAuthResultResponse linkRequired(
            String linkToken,
            String provider,
            String providerUserId,
            String email,
            String nickname
    ) {
        return new KakaoOAuthResultResponse(
                KakaoOAuthResultStatus.LINK_REQUIRED,
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
                0L,
                null,
                null
        );
    }

    public static KakaoOAuthResultResponse error(String errorCode, String errorMessage) {
        return new KakaoOAuthResultResponse(
                KakaoOAuthResultStatus.ERROR,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0L,
                0L,
                errorCode,
                errorMessage
        );
    }
}
