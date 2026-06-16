package com.example.member.verification.infrastructure.redis.emailverification;

import java.time.LocalDateTime;
import java.util.UUID;

public record SignupEmailVerificationToken(
        String token,
        UUID memberId,
        String email,
        LocalDateTime expiresAt
) {
}
