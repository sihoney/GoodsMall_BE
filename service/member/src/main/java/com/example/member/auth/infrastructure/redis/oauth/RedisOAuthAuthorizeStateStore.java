package com.example.member.auth.infrastructure.redis.oauth;

import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisOAuthAuthorizeStateStore implements OAuthAuthorizeStateStore {

    private static final String STATE_KEY_PATTERN = "oauth:%s:state:";
    private static final String RESULT_KEY_PATTERN = "oauth:%s:result:";

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

    @Override
    public Optional<String> createOAuthResult(OAuthProvider provider, OAuthResult result, Duration ttl) {
        String resultKey = UUID.randomUUID().toString();
        String key = buildResultKey(provider, resultKey);

        for (Map.Entry<String, String> entry : OAuthResultSnapshot.from(result).toMap().entrySet()) {
            stringRedisTemplate.opsForHash().put(key, entry.getKey(), entry.getValue());
        }
        stringRedisTemplate.expire(key, ttl);
        return Optional.of(resultKey);
    }

    @Override
    public Optional<OAuthResult> consumeOAuthResult(OAuthProvider provider, String resultKey) {
        String key = buildResultKey(provider, resultKey);
        var entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        stringRedisTemplate.delete(key);

        return Optional.of(OAuthResultSnapshot.fromMap(entries).toOAuthResult());
    }

    private String buildStateKey(OAuthProvider provider, String state) {
        return STATE_KEY_PATTERN.formatted(provider.name().toLowerCase(Locale.ROOT)) + state;
    }

    private String buildResultKey(OAuthProvider provider, String resultKey) {
        return RESULT_KEY_PATTERN.formatted(provider.name().toLowerCase(Locale.ROOT)) + resultKey;
    }

}
