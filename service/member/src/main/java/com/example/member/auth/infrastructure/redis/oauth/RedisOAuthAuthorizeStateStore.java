package com.example.member.auth.infrastructure.redis.oauth;

import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.application.dto.result.OAuthResultStatus;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
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

    // provider profile
    private static final String FIELD_PROVIDER_USER_ID = "providerUserId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NICKNAME = "nickname";
    private static final String FIELD_CREATED_AT = "createdAt";
    // oauth authorize state
    // oauth result
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_LINK_REQUIRED = "linkRequired";
    private static final String FIELD_LINK_TOKEN = "linkToken";
    private static final String FIELD_PROVIDER = "provider";
    private static final String FIELD_ACCESS_TOKEN = "accessToken";
    private static final String FIELD_REFRESH_TOKEN = "refreshToken";
    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_TOKEN_TYPE = "tokenType";
    private static final String FIELD_ACCESS_TOKEN_EXPIRES_IN = "accessTokenExpiresIn";
    private static final String FIELD_REFRESH_TOKEN_EXPIRES_IN = "refreshTokenExpiresIn";
    private static final String FIELD_ERROR_CODE = "errorCode";
    private static final String FIELD_ERROR_MESSAGE = "errorMessage";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> createAuthorizeState(OAuthProvider provider, OAuthAuthorizeState authorizeState, Duration ttl) {
        String key = buildStateKey(provider, authorizeState.state());
        stringRedisTemplate.opsForHash().put(key, FIELD_CREATED_AT, authorizeState.createdAt().toString());
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
        return Optional.of(new OAuthAuthorizeState(
                state,
                Instant.parse((String) entries.get(FIELD_CREATED_AT))
        ));
    }

    @Override
    public Optional<String> createOAuthResult(OAuthProvider provider, OAuthResult result, Duration ttl) {
        String resultKey = UUID.randomUUID().toString();
        String key = buildResultKey(provider, resultKey);

        stringRedisTemplate.opsForHash().put(key, FIELD_STATUS, result.status().name());
        stringRedisTemplate.opsForHash().put(key, FIELD_LINK_REQUIRED, String.valueOf(result.linkRequired()));
        stringRedisTemplate.opsForHash().put(key, FIELD_LINK_TOKEN, nullToEmpty(result.linkToken()));
        stringRedisTemplate.opsForHash().put(key, FIELD_PROVIDER, nullToEmpty(result.provider()));
        stringRedisTemplate.opsForHash().put(key, FIELD_PROVIDER_USER_ID, nullToEmpty(result.providerUserId()));
        stringRedisTemplate.opsForHash().put(key, FIELD_EMAIL, nullToEmpty(result.email()));
        stringRedisTemplate.opsForHash().put(key, FIELD_NICKNAME, nullToEmpty(result.nickname()));
        stringRedisTemplate.opsForHash().put(key, FIELD_ACCESS_TOKEN, nullToEmpty(result.accessToken()));
        stringRedisTemplate.opsForHash().put(key, FIELD_REFRESH_TOKEN, nullToEmpty(result.refreshToken()));
        stringRedisTemplate.opsForHash().put(
                key,
                FIELD_SESSION_ID,
                result.sessionId() == null ? "" : result.sessionId().toString()
        );
        stringRedisTemplate.opsForHash().put(key, FIELD_TOKEN_TYPE, nullToEmpty(result.tokenType()));
        stringRedisTemplate.opsForHash().put(
                key,
                FIELD_ACCESS_TOKEN_EXPIRES_IN,
                String.valueOf(result.accessTokenExpiresIn())
        );
        stringRedisTemplate.opsForHash().put(
                key,
                FIELD_REFRESH_TOKEN_EXPIRES_IN,
                String.valueOf(result.refreshTokenExpiresIn())
        );
        stringRedisTemplate.opsForHash().put(key, FIELD_ERROR_CODE, nullToEmpty(result.errorCode()));
        stringRedisTemplate.opsForHash().put(key, FIELD_ERROR_MESSAGE, nullToEmpty(result.errorMessage()));
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

        return Optional.of(new OAuthResult(
                OAuthResultStatus.valueOf((String) entries.get(FIELD_STATUS)),
                Boolean.parseBoolean((String) entries.get(FIELD_LINK_REQUIRED)),
                emptyToNull((String) entries.get(FIELD_LINK_TOKEN)),
                emptyToNull((String) entries.get(FIELD_PROVIDER)),
                emptyToNull((String) entries.get(FIELD_PROVIDER_USER_ID)),
                emptyToNull((String) entries.get(FIELD_EMAIL)),
                emptyToNull((String) entries.get(FIELD_NICKNAME)),
                emptyToNull((String) entries.get(FIELD_ACCESS_TOKEN)),
                emptyToNull((String) entries.get(FIELD_REFRESH_TOKEN)),
                parseUuid((String) entries.get(FIELD_SESSION_ID)),
                emptyToNull((String) entries.get(FIELD_TOKEN_TYPE)),
                parseLong((String) entries.get(FIELD_ACCESS_TOKEN_EXPIRES_IN)),
                parseLong((String) entries.get(FIELD_REFRESH_TOKEN_EXPIRES_IN)),
                emptyToNull((String) entries.get(FIELD_ERROR_CODE)),
                emptyToNull((String) entries.get(FIELD_ERROR_MESSAGE))
        ));
    }

    private String buildStateKey(OAuthProvider provider, String state) {
        return STATE_KEY_PATTERN.formatted(provider.name().toLowerCase(Locale.ROOT)) + state;
    }

    private String buildResultKey(OAuthProvider provider, String resultKey) {
        return RESULT_KEY_PATTERN.formatted(provider.name().toLowerCase(Locale.ROOT)) + resultKey;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private UUID parseUuid(String value) {
        String normalized = emptyToNull(value);
        return normalized == null ? null : UUID.fromString(normalized);
    }

    private long parseLong(String value) {
        String normalized = emptyToNull(value);
        return normalized == null ? 0L : Long.parseLong(normalized);
    }
}
