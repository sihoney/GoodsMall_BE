package com.example.member.application.dto.result;

import java.util.UUID;

public record KakaoOAuthResult(
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
    public static KakaoOAuthResult success(
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
        return new KakaoOAuthResult(
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

    public static KakaoOAuthResult linkRequired(
            String linkToken,
            String provider,
            String providerUserId,
            String email,
            String nickname
    ) {
        return new KakaoOAuthResult(
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

    public static KakaoOAuthResult error(String errorCode, String errorMessage) {
        return new KakaoOAuthResult(
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
