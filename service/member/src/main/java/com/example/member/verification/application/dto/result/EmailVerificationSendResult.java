package com.example.member.verification.application.dto.result;

import java.time.LocalDateTime;

public record EmailVerificationSendResult(
        String email,
        String purpose,
        String status,
        LocalDateTime expiresAt
) {
}
