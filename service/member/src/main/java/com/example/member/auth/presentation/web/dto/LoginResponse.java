package com.example.member.auth.presentation.web.dto;

import com.example.member.auth.application.dto.result.AuthTokenResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "로그인 응답")
public record LoginResponse(
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
    public static LoginResponse from(AuthTokenResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.tokenType(),
                result.accessTokenExpiresIn(),
                result.refreshTokenExpiresIn(),
                result.sessionId()
        );
    }
}
