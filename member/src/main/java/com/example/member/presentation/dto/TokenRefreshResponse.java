package com.example.member.presentation.dto;

import java.util.UUID;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        UUID sessionId
) {
}
