package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "토큰 재발급 응답")
public record TokenRefreshResponse(
        @Schema(description = "액세스 토큰")
        String accessToken,
        @Schema(description = "리프레시 토큰")
        String refreshToken,
        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "액세스 만료초", example = "3600")
        long accessTokenExpiresIn,
        @Schema(description = "리프레시 만료초", example = "604800")
        long refreshTokenExpiresIn,
        @Schema(description = "인증 세션 ID")
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
