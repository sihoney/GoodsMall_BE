package com.todaylunch.gateway.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisTokenBlacklistStore implements TokenBlacklistStore {

    private static final String ACCESS_TOKEN_PREFIX = "auth:blacklist:access:";
    private static final String SESSION_PREFIX = "auth:blacklist:session:";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean isAccessTokenBlacklisted(String accessTokenId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(ACCESS_TOKEN_PREFIX + accessTokenId));
    }

    @Override
    public boolean isSessionBlacklisted(UUID sessionId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(SESSION_PREFIX + sessionId));
    }
}
