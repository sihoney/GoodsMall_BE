package com.example.member.infrastructure.redis;

import java.time.Instant;

public record KakaoOAuthPendingLink(
        String linkToken,
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl,
        Instant createdAt
) {
}
