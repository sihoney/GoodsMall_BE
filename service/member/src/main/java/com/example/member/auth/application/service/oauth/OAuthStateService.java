package com.example.member.auth.application.service.oauth;

import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.infrastructure.redis.oauth.OAuthAuthorizeState;
import com.example.member.auth.infrastructure.redis.oauth.OAuthAuthorizeStateStore;
import com.example.member.common.config.OAuthProviderProperties;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private final OAuthProviderPropertiesRegistry oauthProviderPropertiesRegistry;
    private final OAuthAuthorizeStateStore oauthAuthorizeStateStore;

    public String createLoginAuthorizeState(OAuthProvider provider) {
        // [1] State 생성
        String state = UUID.randomUUID().toString();
        OAuthProviderProperties properties = oauthProviderPropertiesRegistry.get(provider);

        // [2] 로그인 State 저장
        oauthAuthorizeStateStore.createAuthorizeState(
                provider,
                new OAuthAuthorizeState(
                        state,
                        Instant.now()
                ),
                properties.stateTtl()
        );

        // [3] State 반환
        return state;
    }

    public OAuthAuthorizeState consumeAuthorizeState(OAuthProvider provider, String state) {
        // [1] State 조회
        return oauthAuthorizeStateStore.consumeAuthorizeState(provider, normalizeRequired(state, "state"))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OAuth state."));
    }

    public String createOAuthResultKey(OAuthProvider provider, OAuthResult result) {
        // [1] 결과 저장
        OAuthProviderProperties properties = oauthProviderPropertiesRegistry.get(provider);
        return oauthAuthorizeStateStore.createOAuthResult(provider, result, properties.resultTtl())
                .orElseThrow(() -> new IllegalStateException("OAuth result could not be stored."));
    }

    public OAuthResult consumeOAuthResult(OAuthProvider provider, String resultKey) {
        // [1] 결과 조회
        return oauthAuthorizeStateStore.consumeOAuthResult(provider, normalizeRequired(resultKey, "resultKey"))
                .orElseThrow(() -> new IllegalStateException("OAuth result does not exist or has expired."));
    }

    public String getFrontendCallbackUrl(OAuthProvider provider) {
        // [1] Callback URL 조회
        return normalizeRequired(oauthProviderPropertiesRegistry.get(provider).frontendCallbackUrl(), "frontendCallbackUrl");
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }
}
