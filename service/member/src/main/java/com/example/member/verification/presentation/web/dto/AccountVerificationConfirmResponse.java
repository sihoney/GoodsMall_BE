package com.example.member.verification.presentation.web.dto;

import com.example.member.verification.application.dto.result.AccountVerificationConfirmResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "계좌 인증 확인 응답")
public record AccountVerificationConfirmResponse(
        @Schema(description = "인증 세션 ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "인증 성공", example = "true")
        boolean verified,
        @Schema(description = "인증 상태", example = "VERIFIED")
        String status,
        @Schema(description = "완료 시각")
        LocalDateTime verifiedAt,
        @Schema(description = "실패 횟수", example = "0")
        int attemptCount,
        @Schema(description = "인증 토큰")
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

    @Schema(description = "토큰 묶음")
    public record AuthTokens(
            @Schema(description = "액세스 토큰")
            String accessToken,
            @Schema(description = "리프레시 토큰")
            String refreshToken,
            @Schema(description = "토큰 타입", example = "Bearer")
            String tokenType,
            @Schema(description = "액세스 만료ms", example = "3600000")
            long accessTokenExpiresIn,
            @Schema(description = "리프레시 만료ms", example = "1209600000")
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
