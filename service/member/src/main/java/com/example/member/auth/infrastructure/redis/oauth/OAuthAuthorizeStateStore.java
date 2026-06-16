package com.example.member.auth.infrastructure.redis.oauth;

import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.time.Duration;
import java.util.Optional;

public interface OAuthAuthorizeStateStore {

    Optional<String> createAuthorizeState(OAuthProvider provider, OAuthAuthorizeState authorizeState, Duration ttl);

    Optional<OAuthAuthorizeState> consumeAuthorizeState(OAuthProvider provider, String state);
}
