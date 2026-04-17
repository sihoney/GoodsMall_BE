package com.example.member.infrastructure.redis;

import java.util.UUID;

public record AuthSession(
        UUID memberId,
        UUID sessionId,
        String refreshTokenId
) {
}
