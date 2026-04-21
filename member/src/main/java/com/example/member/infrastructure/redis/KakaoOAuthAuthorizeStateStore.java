package com.example.member.infrastructure.redis;

import com.example.member.presentation.dto.KakaoOAuthResultResponse;
import java.time.Duration;
import java.util.Optional;

public interface KakaoOAuthAuthorizeStateStore {

    Optional<String> createAuthorizeState(KakaoOAuthAuthorizeState authorizeState, Duration ttl);

    Optional<KakaoOAuthAuthorizeState> consumeAuthorizeState(String state);

    Optional<String> createPendingLink(KakaoOAuthPendingLink pendingLink, Duration ttl);

    Optional<KakaoOAuthPendingLink> consumePendingLink(String linkToken);

    Optional<String> createOAuthResult(KakaoOAuthResultResponse result, Duration ttl);

    Optional<KakaoOAuthResultResponse> consumeOAuthResult(String resultKey);
}
