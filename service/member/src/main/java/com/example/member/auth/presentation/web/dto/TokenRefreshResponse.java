package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.AuthTokenResult;
import java.util.UUID;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UUID sessionId
) {
    public static TokenRefreshResponse from(AuthTokenResult result) {
        return new TokenRefreshResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn(),
                result.sessionId()
        );
    }
}

