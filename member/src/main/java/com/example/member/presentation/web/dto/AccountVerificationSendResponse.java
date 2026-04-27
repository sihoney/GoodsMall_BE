package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.AccountVerificationSendResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "모의 계좌 인증 발급 응답")
public record AccountVerificationSendResponse(
        @Schema(description = "계좌 인증 세션 ID", example = "av_01J4XYZ")
        String sessionId,
        @Schema(description = "인증 상태", example = "PENDING")
        String status,
        @Schema(description = "마스킹된 계좌번호", example = "123-****-0123")
        String maskedAccountNumber,
        @Schema(description = "서버가 생성한 인증 코드", example = "482931")
        String verificationCode,
        @Schema(description = "세션 만료 시각")
        LocalDateTime expiresAt,
        @Schema(description = "누적 인증 실패 횟수", example = "0")
        int attemptCount,
        @Schema(description = "인증 코드 재전송 횟수", example = "0")
        int resendCount
) {
    public static AccountVerificationSendResponse from(AccountVerificationSendResult result) {
        return new AccountVerificationSendResponse(
                result.sessionId(),
                result.status(),
                result.maskedAccountNumber(),
                result.verificationCode(),
                result.expiresAt(),
                result.attemptCount(),
                result.resendCount()
        );
    }
}
