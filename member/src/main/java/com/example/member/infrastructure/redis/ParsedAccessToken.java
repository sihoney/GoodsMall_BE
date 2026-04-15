package com.example.member.infrastructure.redis;

import java.time.Instant;
import java.util.UUID;

public record ParsedAccessToken(
        UUID memberId,
        UUID sessionId,
        String accessTokenId,
        Instant expiresAt
) {
}
