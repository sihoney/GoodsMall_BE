package com.example.member.auth.infrastructure.redis.oauth;

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
