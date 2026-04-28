package com.example.member.infrastructure.redis.auth;

import java.util.UUID;

public record AuthSession(
        UUID memberId,
        UUID sessionId,
        String refreshTokenId
) {
}
