package com.example.member.infrastructure.redis.emailverification;

import java.time.Instant;
import java.util.UUID;

public record EmailVerificationAutoLoginToken(
        String token,
        UUID memberId,
        Instant createdAt
) {
}
