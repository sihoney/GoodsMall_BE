package com.todaylunch.gateway.auth;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSessionWhitelistStore implements SessionWhitelistStore {

    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String MEMBER_ID_FIELD = "memberId";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean hasSession(UUID memberId, UUID sessionId) {
        String storedMemberId = (String) stringRedisTemplate.opsForHash()
                .get(SESSION_KEY_PREFIX + sessionId, MEMBER_ID_FIELD);

        return memberId.toString().equals(storedMemberId);
    }
}

