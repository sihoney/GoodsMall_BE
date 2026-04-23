package com.example.member.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Mock account verification confirm response")
public record AccountVerificationConfirmResponse(
        @Schema(description = "Account verification session ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "Whether verification succeeded", example = "true")
        boolean verified,
        @Schema(description = "Verification status", example = "VERIFIED")
        String status,
        @Schema(description = "Verification completion timestamp")
        LocalDateTime verifiedAt,
        @Schema(description = "Failed attempt count", example = "0")
        int attemptCount,
        @Schema(description = "New auth tokens issued after seller promotion")
        AuthTokens auth
) {
    public record AuthTokens(
            @Schema(description = "JWT access token")
            String accessToken,
            @Schema(description = "JWT refresh token")
            String refreshToken,
            @Schema(description = "Token type", example = "Bearer")
            String tokenType,
            @Schema(description = "Access token expiration in milliseconds", example = "3600000")
            long accessTokenExpiresIn,
            @Schema(description = "Refresh token expiration in milliseconds", example = "1209600000")
            long refreshTokenExpiresIn,
            @Schema(description = "Auth session ID")
            UUID sessionId
    ) {
    }
}
