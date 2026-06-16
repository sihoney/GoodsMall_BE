package com.example.member.verification.infrastructure.redis.emailverification;

import com.example.member.verification.application.port.out.EmailVerificationTokenStore;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * Redis schema
 *
 * [1] 회원가입 이메일 인증 토큰
 * - key: auth:email-verification:signup:token:{token}
 * - type: Hash
 * - ttl: 이메일 인증 토큰 만료 시간
 * - value:
 *   - memberId: 회원 ID
 *   - email: 인증 대상 이메일
 *   - expiresAt: 인증 만료 시각
 *
 * [2] 회원별 현재 이메일 인증 토큰
 * - key: auth:email-verification:signup:member:{memberId}
 * - type: String
 * - ttl: 이메일 인증 토큰 만료 시간
 * - value: token
 *
 * [3] 저장 예시
 * - auth:email-verification:signup:token:email-token-abc123
 *   memberId = 11111111-1111-1111-1111-111111111111
 *   email = user@example.com
 *   expiresAt = 2026-06-16T14:35:00
 *
 * - auth:email-verification:signup:member:11111111-1111-1111-1111-111111111111
 *   value = email-token-abc123
 */
@Component
@RequiredArgsConstructor
public class RedisEmailVerificationTokenStore implements EmailVerificationTokenStore {

    private static final String TOKEN_KEY_PREFIX = "auth:email-verification:signup:token:";
    private static final String MEMBER_KEY_PREFIX = "auth:email-verification:signup:member:";
    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_EXPIRES_AT = "expiresAt";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveSignupToken(SignupEmailVerificationToken token, Duration ttl) {
        deleteByMemberId(token.memberId());

        String tokenKey = buildTokenKey(token.token());
        stringRedisTemplate.opsForHash().put(tokenKey, FIELD_MEMBER_ID, token.memberId().toString());
        stringRedisTemplate.opsForHash().put(tokenKey, FIELD_EMAIL, token.email());
        stringRedisTemplate.opsForHash().put(tokenKey, FIELD_EXPIRES_AT, token.expiresAt().toString());
        stringRedisTemplate.expire(tokenKey, ttl);

        String memberKey = buildMemberKey(token.memberId());
        stringRedisTemplate.opsForValue().set(memberKey, token.token(), ttl);
    }

    @Override
    public Optional<SignupEmailVerificationToken> findSignupToken(String token) {
        var entries = stringRedisTemplate.opsForHash().entries(buildTokenKey(token));
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new SignupEmailVerificationToken(
                token,
                UUID.fromString((String) entries.get(FIELD_MEMBER_ID)),
                (String) entries.get(FIELD_EMAIL),
                LocalDateTime.parse((String) entries.get(FIELD_EXPIRES_AT))
        ));
    }

    @Override
    public void deleteByToken(String token) {
        Optional<SignupEmailVerificationToken> existingToken = findSignupToken(token);
        stringRedisTemplate.delete(buildTokenKey(token));
        existingToken.ifPresent(value -> stringRedisTemplate.delete(buildMemberKey(value.memberId())));
    }

    @Override
    public void deleteByMemberId(UUID memberId) {
        String memberKey = buildMemberKey(memberId);
        String token = stringRedisTemplate.opsForValue().get(memberKey);
        if (token != null && !token.isBlank()) {
            stringRedisTemplate.delete(buildTokenKey(token));
        }
        stringRedisTemplate.delete(memberKey);
    }

    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    private String buildMemberKey(UUID memberId) {
        return MEMBER_KEY_PREFIX + memberId;
    }
}
