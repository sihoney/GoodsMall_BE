package com.example.member.seller.application.service;

import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.seller.application.port.out.SellerEventPort;
import com.example.member.seller.application.port.out.SellerPersistencePort;
import com.example.member.verification.exception.AccountVerificationNotAllowedException;
import com.example.member.verification.exception.AccountVerificationNotFoundException;
import com.example.member.member.exception.MemberNotFoundException;
import com.example.member.member.domain.entity.Member;
import com.example.member.seller.domain.entity.Seller;
import com.example.member.seller.infrastructure.crypto.AccountEncryptionService;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSession;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSessionStore;
import com.example.member.seller.infrastructure.redis.seller.SellerDraft;
import com.example.member.seller.infrastructure.redis.seller.SellerDraftStore;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerPromotionService {

    private final AccountVerificationSessionStore sessionStore;
    private final SellerDraftStore sellerDraftStore;
    private final SellerPersistencePort sellerPersistencePort;
    private final MemberPersistencePort memberPersistencePort;
    private final AccountEncryptionService accountEncryptionService;
    private final SellerEventPort sellerEventPort;

    @Transactional
    public void promoteAfterAccountVerified(UUID memberId, String sessionId) {
        AccountVerificationSession session = sessionStore.findSession(sessionId)
                .orElseThrow(AccountVerificationNotFoundException::new);
        if (!session.belongsTo(memberId)) {
            throw new AccountVerificationNotAllowedException("계좌 인증 세션이 현재 회원에게 속하지 않습니다.");
        }
        if (!session.isVerified()) {
            throw new AccountVerificationNotAllowedException("계좌 인증 세션이 아직 검증되지 않았습니다.");
        }

        SellerDraft draft = sellerDraftStore.findDraft(session.getDraftId())
                .orElseThrow(() -> new IllegalStateException("판매자 등록 초안이 존재하지 않습니다."));

        if (sellerPersistencePort.existsByMemberId(memberId)) {
            sellerDraftStore.deleteDraft(draft.getDraftId());
            sellerDraftStore.deleteCurrentDraft(memberId);
            return;
        }

        Member member = memberPersistencePort.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        String accountNumber = accountEncryptionService.decrypt(draft.getEncryptedAccountNumber());
        Seller seller = Seller.create(
                UUID.randomUUID(),
                memberId,
                draft.getBankName(),
                accountNumber,
                now
        );

        sellerPersistencePort.save(seller);
        member.changeRole(MemberRole.SELLER, now);
        sellerEventPort.publishSellerPromoted(member, seller);

        sellerDraftStore.deleteDraft(draft.getDraftId());
        sellerDraftStore.deleteCurrentDraft(memberId);
    }
}
