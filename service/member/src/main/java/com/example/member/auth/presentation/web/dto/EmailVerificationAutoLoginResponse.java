package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.AuthTokenResult;
import java.util.UUID;

public record EmailVerificationAutoLoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UUID sessionId
) {

    public static EmailVerificationAutoLoginResponse from(AuthTokenResult result) {
        return new EmailVerificationAutoLoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn(),
                result.sessionId()
        );
    }
}
