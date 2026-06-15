package com.example.member.auth.infrastructure.redis.passwordreset;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetToken(
        String token,
        UUID memberId,
        String email,
        Instant createdAt
) {
}
