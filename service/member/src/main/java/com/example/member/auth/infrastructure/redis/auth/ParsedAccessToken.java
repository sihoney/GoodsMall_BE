package com.example.member.auth.infrastructure.redis.auth;

import java.time.Instant;
import java.util.UUID;

public record ParsedAccessToken(
        UUID memberId,
        UUID sessionId,
        String accessTokenId,
        Instant expiresAt
) {
}
