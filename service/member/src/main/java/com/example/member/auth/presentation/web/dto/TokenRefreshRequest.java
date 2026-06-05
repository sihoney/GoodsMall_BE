package com.example.member.auth.presentation.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 요청")
public record TokenRefreshRequest(
        @Schema(description = "리프레시 토큰")
        String refreshToken
) {
}
