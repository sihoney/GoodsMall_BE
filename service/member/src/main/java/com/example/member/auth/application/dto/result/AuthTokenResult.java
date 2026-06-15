package com.example.member.auth.application.dto.result;

import java.util.UUID;

public record AuthTokenResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UUID sessionId
) {
}
