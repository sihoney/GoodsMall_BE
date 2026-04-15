package com.example.member.infrastructure.redis;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisKakaoOAuthAuthorizeStateStore implements KakaoOAuthAuthorizeStateStore {

    private static final String STATE_KEY_PREFIX = "oauth:kakao:state:";
    private static final String PENDING_LINK_KEY_PREFIX = "oauth:kakao:pending-link:";
    private static final String FIELD_PROVIDER_USER_ID = "providerUserId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_NICKNAME = "nickname";
    private static final String FIELD_PROFILE_IMAGE_URL = "profileImageUrl";
    private static final String FIELD_CREATED_AT = "createdAt";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void create(String state, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildStateKey(state), "1", ttl);
    }

    @Override
    public boolean consume(String state) {
        Boolean deleted = stringRedisTemplate.delete(buildStateKey(state));
        return Boolean.TRUE.equals(deleted);
    }

    @Override
    public Optional<String> createPendingLink(KakaoOAuthPendingLink pendingLink, Duration ttl) {
        String key = buildPendingLinkKey(pendingLink.linkToken());
        stringRedisTemplate.opsForHash().put(key, FIELD_PROVIDER_USER_ID, pendingLink.providerUserId());
        stringRedisTemplate.opsForHash().put(key, FIELD_EMAIL, nullToEmpty(pendingLink.email()));
        stringRedisTemplate.opsForHash().put(key, FIELD_NICKNAME, nullToEmpty(pendingLink.nickname()));
        stringRedisTemplate.opsForHash().put(key, FIELD_PROFILE_IMAGE_URL, nullToEmpty(pendingLink.profileImageUrl()));
        stringRedisTemplate.opsForHash().put(key, FIELD_CREATED_AT, pendingLink.createdAt().toString());
        stringRedisTemplate.expire(key, ttl);
        return Optional.of(pendingLink.linkToken());
    }

    @Override
    public Optional<KakaoOAuthPendingLink> consumePendingLink(String linkToken) {
        String key = buildPendingLinkKey(linkToken);
        var entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        stringRedisTemplate.delete(key);
        return Optional.of(new KakaoOAuthPendingLink(
                linkToken,
                (String) entries.get(FIELD_PROVIDER_USER_ID),
                emptyToNull((String) entries.get(FIELD_EMAIL)),
                emptyToNull((String) entries.get(FIELD_NICKNAME)),
                emptyToNull((String) entries.get(FIELD_PROFILE_IMAGE_URL)),
                Instant.parse((String) entries.get(FIELD_CREATED_AT))
        ));
    }

    private String buildStateKey(String state) {
        return STATE_KEY_PREFIX + state;
    }

    private String buildPendingLinkKey(String linkToken) {
        return PENDING_LINK_KEY_PREFIX + linkToken;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
