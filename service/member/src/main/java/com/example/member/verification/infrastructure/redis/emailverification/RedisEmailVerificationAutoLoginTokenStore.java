package com.example.member.verification.infrastructure.redis.emailverification;


import com.example.member.verification.application.port.out.EmailVerificationAutoLoginTokenStore;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * Redis schema
 *
 * [1] 이메일 인증 후 자동 로그인 토큰
 * - key: auth:email-verification:auto-login:{token}
 * - type: Hash
 * - ttl: 자동 로그인 토큰 만료 시간
 * - value:
 *   - memberId: 회원 ID
 *   - createdAt: 토큰 생성 시각
 *
 * [2] 저장 예시
 * - auth:email-verification:auto-login:auto-login-token-abc123
 *   memberId = 11111111-1111-1111-1111-111111111111
 *   createdAt = 2026-06-16T14:30:00Z
 */
@Component
@RequiredArgsConstructor
public class RedisEmailVerificationAutoLoginTokenStore implements EmailVerificationAutoLoginTokenStore {

    private static final String KEY_PREFIX = "auth:email-verification:auto-login:";
    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_CREATED_AT = "createdAt";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<String> create(EmailVerificationAutoLoginToken token, Duration ttl) {
        String key = buildKey(token.token());
        stringRedisTemplate.opsForHash().put(key, FIELD_MEMBER_ID, token.memberId().toString());
        stringRedisTemplate.opsForHash().put(key, FIELD_CREATED_AT, token.createdAt().toString());
        stringRedisTemplate.expire(key, ttl);
        return Optional.of(token.token());
    }

    @Override
    public Optional<EmailVerificationAutoLoginToken> consume(String token) {
        String key = buildKey(token);
        var entries = stringRedisTemplate.opsForHash().entries(key);
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        stringRedisTemplate.delete(key);
        return Optional.of(new EmailVerificationAutoLoginToken(
                token,
                UUID.fromString((String) entries.get(FIELD_MEMBER_ID)),
                Instant.parse((String) entries.get(FIELD_CREATED_AT))
        ));
    }

    private String buildKey(String token) {
        return KEY_PREFIX + token;
    }
}
