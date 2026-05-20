package com.example.member.application.dto.result;

import java.time.LocalDateTime;

public record AccountVerificationCurrentResult(
        String sessionId,
        String status,
        String bankName,
        String maskedAccountNumber,
        LocalDateTime expiresAt,
        int attemptCount,
        int resendCount
) {
}
