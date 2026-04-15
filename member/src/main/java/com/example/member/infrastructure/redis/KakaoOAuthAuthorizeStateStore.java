package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.util.Optional;

public interface KakaoOAuthAuthorizeStateStore {

    void create(String state, Duration ttl);

    boolean consume(String state);

    Optional<String> createPendingLink(KakaoOAuthPendingLink pendingLink, Duration ttl);

    Optional<KakaoOAuthPendingLink> consumePendingLink(String linkToken);
}
