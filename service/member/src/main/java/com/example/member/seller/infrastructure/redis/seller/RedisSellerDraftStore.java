package com.example.member.seller.infrastructure.redis.seller;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/*
 * Redis schema
 *
 * [1] 판매자 등록 draft
 * - key: seller-draft:draft:{draftId}
 * - type: Hash
 * - ttl: 판매자 등록 draft 만료 시간
 * - value:
 *   - draftId: 판매자 draft ID
 *   - memberId: 회원 ID
 *   - sessionId: 계좌 인증 세션 ID
 *   - bankName: 은행명
 *   - encryptedAccountNumber: 암호화된 계좌번호
 *   - accountNumberMasked: 마스킹된 계좌번호
 *   - status: draft 상태
 *   - createdAt: draft 생성 시각
 *   - updatedAt: draft 수정 시각
 *
 * [2] 회원별 현재 draft
 * - key: seller-draft:member:{memberId}:current
 * - type: String
 * - ttl: 판매자 등록 draft 만료 시간
 * - value: draftId
 *
 * [3] 저장 예시
 * - seller-draft:draft:ad_abc123
 *   draftId = ad_abc123
 *   memberId = 11111111-1111-1111-1111-111111111111
 *   sessionId = av_abc123
 *   bankName = 국민은행
 *   encryptedAccountNumber = encrypted-value
 *   accountNumberMasked = 123-****-7890
 *   status = PENDING
 *   createdAt = 2026-06-16T14:30:00
 *   updatedAt = 2026-06-16T14:30:00
 *
 * - seller-draft:member:11111111-1111-1111-1111-111111111111:current
 *   value = ad_abc123
 */
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
