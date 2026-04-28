package com.example.member.infrastructure.redis.seller;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface SellerDraftStore {

    void saveDraft(SellerDraft draft, Duration ttl);

    Optional<SellerDraft> findDraft(String draftId);

    Optional<String> findCurrentDraftId(UUID memberId);

    void saveCurrentDraft(UUID memberId, String draftId, Duration ttl);

    void deleteDraft(String draftId);

    void deleteCurrentDraft(UUID memberId);
}
