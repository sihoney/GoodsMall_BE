package com.example.member.infrastructure.redis;

import java.util.UUID;

public record ParsedRefreshToken(
        UUID memberId,
        UUID sessionId,
        String refreshTokenId
) {
}
