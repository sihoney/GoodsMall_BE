package com.example.member.presentation.web.dto;

import java.time.LocalDateTime;

public record EmailVerificationSendResponse(
        String email,
        String purpose,
        String status,
        LocalDateTime expiresAt
) {
}

