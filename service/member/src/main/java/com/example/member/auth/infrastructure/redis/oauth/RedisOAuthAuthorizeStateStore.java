package com.example.member.auth.infrastructure.redis.oauth;

import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * Redis schema
 *
 * [1] OAuth 인가 state
 * - key: oauth:{provider}:state:{state}
 * - type: Hash
 * - ttl: OAuth state 만료 시간
 * - value:
 *   - createdAt: state 생성 시각
 *
 * [2] 저장 예시
 * - oauth:kakao:state:state_abc123
 *   createdAt = 2026-06-16T14:30:00Z
 */
@Component
@RequiredArgsConstructor
public class RedisOAuthAuthorizeStateStore implements OAuthAuthorizeStateStore {

    private static final String STATE_KEY_PATTERN = "oauth:%s:state:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> createAuthorizeState(OAuthProvider provider, OAuthAuthorizeState authorizeState, Duration ttl) {
        String key = buildStateKey(provider, authorizeState.state());
        for (Map.Entry<String, String> entry : authorizeState.toMap().entrySet()) {
            stringRedisTemplate.opsForHash().put(key, entry.getKey(), entry.getValue());
        }
        stringRedisTemplate.expire(key, ttl);
        return Optional.of(authorizeState.state());
    }

    @Override
    public Optional<OAuthAuthorizeState> consumeAuthorizeState(OAuthProvider provider, String state) {
        String key = buildStateKey(provider, state);
        var entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        stringRedisTemplate.delete(key);
        return Optional.of(OAuthAuthorizeState.fromMap(state, entries));
    }

    private String buildStateKey(OAuthProvider provider, String state) {
        return STATE_KEY_PATTERN.formatted(provider.name().toLowerCase(Locale.ROOT)) + state;
    }
}
