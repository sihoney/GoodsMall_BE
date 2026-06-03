package com.example.member.auth.application.dto.result;

import java.util.UUID;

public record OAuthResult(
        OAuthResultStatus status,
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
    public static OAuthResult success(
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
        return new OAuthResult(
                OAuthResultStatus.SUCCESS,
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

    public static OAuthResult error(String errorCode, String errorMessage) {
        return new OAuthResult(
                OAuthResultStatus.ERROR,
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
