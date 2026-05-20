package com.example.member.infrastructure.redis.oauth;

import java.time.Instant;
import java.util.UUID;

public record KakaoOAuthAuthorizeState(
        String state,
        KakaoOAuthFlowType flowType,
        UUID memberId,
        Instant createdAt
) {
}
