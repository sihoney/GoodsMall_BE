package com.example.member.verification.presentation.web.dto;

import com.example.member.verification.application.dto.result.AccountVerificationCancelResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "모의 계좌 인증 취소 응답")
public record AccountVerificationCancelResponse(
        @Schema(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "인증 상태", example = "CANCELLED")
        String status,
        @Schema(description = "취소 시각")
        LocalDateTime cancelledAt
) {
    public static AccountVerificationCancelResponse from(AccountVerificationCancelResult result) {
        return new AccountVerificationCancelResponse(
                result.sessionId(),
                result.status(),
                result.cancelledAt()
        );
    }
}
