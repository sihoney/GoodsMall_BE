package com.example.member.application.dto.result;

import java.time.LocalDateTime;

public record AccountVerificationSendResult(
        String sessionId,
        String status,
        String maskedAccountNumber,
        String verificationCode,
        LocalDateTime expiresAt,
        int attemptCount,
        int resendCount
) {
}
