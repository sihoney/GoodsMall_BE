package com.example.member.application.dto.result;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountVerificationConfirmResult(
        String sessionId,
        boolean verified,
        String status,
        LocalDateTime verifiedAt,
        int attemptCount,
        AuthTokens auth
) {
    public record AuthTokens(
            String accessToken,
            String refreshToken,
            String tokenType,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn,
            UUID sessionId
    ) {
    }
}
