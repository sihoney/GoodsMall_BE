package com.example.member.infrastructure.redis;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetToken(
        String token,
        UUID memberId,
        String email,
        Instant createdAt
) {
}
