package com.example.member.infrastructure.redis.auth;

import java.util.UUID;

public record ParsedRefreshToken(
        UUID memberId,
        UUID sessionId,
        String refreshTokenId
) {
}
