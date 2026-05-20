package com.example.member.infrastructure.redis.auth;

import java.time.Instant;
import java.util.UUID;

public record AuthSession(
        UUID memberId,
        UUID sessionId,
        String refreshTokenId,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant lastRefreshedAt,
        String userAgent,
        String ipAddress
) {
}
