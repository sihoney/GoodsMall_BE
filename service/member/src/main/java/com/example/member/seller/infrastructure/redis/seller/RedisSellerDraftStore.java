package com.example.member.seller.infrastructure.redis.seller;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSellerDraftStore implements SellerDraftStore {

    private static final String DRAFT_KEY_PREFIX = "seller-draft:draft:";
    private static final String MEMBER_CURRENT_KEY_PREFIX = "seller-draft:member:";
    private static final String MEMBER_CURRENT_KEY_SUFFIX = ":current";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void saveDraft(SellerDraft draft, Duration ttl) {
        String key = buildDraftKey(draft.getDraftId());
        HashOperations<String, Object, Object> hashOperations = stringRedisTemplate.opsForHash();
        for (Map.Entry<String, String> entry : draft.toMap().entrySet()) {
            hashOperations.put(key, entry.getKey(), entry.getValue());
        }
        if (ttl.isZero() || ttl.isNegative()) {
            stringRedisTemplate.delete(key);
            return;
        }
        stringRedisTemplate.expire(key, ttl);
    }

    @Override
    public Optional<SellerDraft> findDraft(String draftId) {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(buildDraftKey(draftId));
        if (entries == null || entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(SellerDraft.fromMap(entries));
    }

    @Override
    public Optional<String> findCurrentDraftId(UUID memberId) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(buildCurrentKey(memberId)));
    }

    @Override
    public void saveCurrentDraft(UUID memberId, String draftId, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildCurrentKey(memberId), draftId, ttl);
    }

    @Override
    public void deleteDraft(String draftId) {
        stringRedisTemplate.delete(buildDraftKey(draftId));
    }

    @Override
    public void deleteCurrentDraft(UUID memberId) {
        stringRedisTemplate.delete(buildCurrentKey(memberId));
    }

    private String buildDraftKey(String draftId) {
        return DRAFT_KEY_PREFIX + draftId;
    }

    private String buildCurrentKey(UUID memberId) {
        return MEMBER_CURRENT_KEY_PREFIX + memberId + MEMBER_CURRENT_KEY_SUFFIX;
    }
}
