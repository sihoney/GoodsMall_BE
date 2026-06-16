package com.example.member.auth.application.dto.result;

public record OAuthCallbackResult(
        boolean success,
        String refreshToken,
        long refreshTokenExpiresIn,
        String errorCode,
        String errorMessage
) {
    public static OAuthCallbackResult success(OAuthResult result) {
        return new OAuthCallbackResult(
                true,
                result.refreshToken(),
                result.refreshTokenExpiresIn(),
                null,
                null
        );
    }

    public static OAuthCallbackResult error(OAuthResult result) {
        return new OAuthCallbackResult(
                false,
                null,
                0L,
                result.errorCode(),
                result.errorMessage()
        );
    }
}
