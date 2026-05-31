package com.example.member.verification.presentation.web.dto;

import com.example.member.verification.application.dto.result.AccountVerificationCurrentResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "현재 계좌 인증 응답")
public record AccountVerificationCurrentResponse(
        @Schema(description = "인증 세션 ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "인증 상태", example = "PENDING")
        String status,
        @Schema(description = "은행명", example = "KAKAO")
        String bankName,
        @Schema(description = "마스킹 계좌", example = "123-****-0123")
        String maskedAccountNumber,
        @Schema(description = "만료 시각")
        LocalDateTime expiresAt,
        @Schema(description = "실패 횟수", example = "1")
        int attemptCount,
        @Schema(description = "재전송 횟수", example = "1")
        int resendCount
) {
    public static AccountVerificationCurrentResponse from(AccountVerificationCurrentResult result) {
        return new AccountVerificationCurrentResponse(
                result.sessionId(),
                result.status(),
                result.bankName(),
                result.maskedAccountNumber(),
                result.expiresAt(),
                result.attemptCount(),
                result.resendCount()
        );
    }
}
