package com.example.member.infrastructure.redis.oauth;

import com.example.member.application.dto.result.KakaoOAuthResult;
import java.time.Duration;
import java.util.Optional;

public interface KakaoOAuthAuthorizeStateStore {

    Optional<String> createAuthorizeState(KakaoOAuthAuthorizeState authorizeState, Duration ttl);

    Optional<KakaoOAuthAuthorizeState> consumeAuthorizeState(String state);

    Optional<String> createPendingLink(KakaoOAuthPendingLink pendingLink, Duration ttl);

    Optional<KakaoOAuthPendingLink> consumePendingLink(String linkToken);

    Optional<String> createOAuthResult(KakaoOAuthResult result, Duration ttl);

    Optional<KakaoOAuthResult> consumeOAuthResult(String resultKey);
}
