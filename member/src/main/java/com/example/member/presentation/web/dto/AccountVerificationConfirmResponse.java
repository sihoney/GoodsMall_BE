package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.AccountVerificationConfirmResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "모의 계좌 인증 확인 응답")
public record AccountVerificationConfirmResponse(
        @Schema(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "인증 성공 여부", example = "true")
        boolean verified,
        @Schema(description = "인증 상태", example = "VERIFIED")
        String status,
        @Schema(description = "인증 완료 시각")
        LocalDateTime verifiedAt,
        @Schema(description = "실패 횟수", example = "0")
        int attemptCount,
        @Schema(description = "판매자 권한 발급 후 반환되는 인증 토큰")
        AuthTokens auth
) {
    public static AccountVerificationConfirmResponse from(AccountVerificationConfirmResult result) {
        return new AccountVerificationConfirmResponse(
                result.sessionId(),
                result.verified(),
                result.status(),
                result.verifiedAt(),
                result.attemptCount(),
                result.auth() == null ? null : AuthTokens.from(result.auth())
        );
    }

    public record AuthTokens(
            @Schema(description = "JWT access token")
            String accessToken,
            @Schema(description = "JWT refresh token")
            String refreshToken,
            @Schema(description = "Token type", example = "Bearer")
            String tokenType,
            @Schema(description = "Access token 만료 시간(ms)", example = "3600000")
            long accessTokenExpiresIn,
            @Schema(description = "Refresh token 만료 시간(ms)", example = "1209600000")
            long refreshTokenExpiresIn,
            @Schema(description = "인증 세션 ID")
            UUID sessionId
    ) {
        public static AuthTokens from(AccountVerificationConfirmResult.AuthTokens result) {
            return new AuthTokens(
                    result.accessToken(),
                    result.refreshToken(),
                    result.tokenType(),
                    result.accessTokenExpiresIn(),
                    result.refreshTokenExpiresIn(),
                    result.sessionId()
            );
        }
    }
}
