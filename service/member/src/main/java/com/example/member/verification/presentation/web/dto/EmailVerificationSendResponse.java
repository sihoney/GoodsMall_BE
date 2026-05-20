package com.example.member.verification.presentation.web.dto;

import java.time.LocalDateTime;

public record EmailVerificationSendResponse(
        String email,
        String purpose,
        String status,
        LocalDateTime expiresAt
) {
}

