package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.OAuthResult;
import java.util.UUID;

public record OAuthResultResponse(
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
    public static OAuthResultResponse from(OAuthResult result) {
        return new OAuthResultResponse(
                OAuthResultStatus.valueOf(result.status().name()),
                result.linkRequired(),
                result.linkToken(),
                result.provider(),
                result.providerUserId(),
                result.email(),
                result.nickname(),
                result.accessToken(),
                result.refreshToken(),
                result.sessionId(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn(),
                result.errorCode(),
                result.errorMessage()
        );
    }

    public static OAuthResultResponse error(String errorCode, String errorMessage) {
        return new OAuthResultResponse(
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

