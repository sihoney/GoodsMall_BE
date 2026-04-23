package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.application.event.MemberEventPublisher;
import com.example.member.common.exception.AccountVerificationNotAllowedException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.Seller;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.crypto.AccountEncryptionService;
import com.example.member.infrastructure.redis.AccountVerificationSession;
import com.example.member.infrastructure.redis.AccountVerificationSessionStore;
import com.example.member.infrastructure.redis.SellerDraft;
import com.example.member.infrastructure.redis.SellerDraftStore;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.infrastructure.repository.SellerRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerPromotionServiceTest {

    @Mock
    private AccountVerificationSessionStore sessionStore;

    @Mock
    private SellerDraftStore sellerDraftStore;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AccountEncryptionService accountEncryptionService;

    @Mock
    private MemberEventPublisher memberEventPublisher;

    @Test
    void promoteAfterAccountVerified_success_createsSellerAndUpdatesMemberRole() {
        SellerPromotionService service = new SellerPromotionService(
                sessionStore,
                sellerDraftStore,
                sellerRepository,
                memberRepository,
                accountEncryptionService,
                memberEventPublisher
        );

        UUID memberId = UUID.randomUUID();
        String sessionId = "av_test_session";
        String draftId = "ad_test_draft";
        LocalDateTime now = LocalDateTime.now();
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                draftId,
                "code-hash",
                now.minusMinutes(1),
                now.plusMinutes(4)
        );
        session.markVerified(now);
        SellerDraft draft = SellerDraft.create(
                draftId,
                memberId,
                sessionId,
                "KAKAO",
                "encrypted-account-number",
                "123-****-0123",
                now
        );
        Member member = createMember(memberId);

        when(sessionStore.findSession(sessionId)).thenReturn(Optional.of(session));
        when(sellerDraftStore.findDraft(draftId)).thenReturn(Optional.of(draft));
        when(sellerRepository.existsByMemberId(memberId)).thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(accountEncryptionService.decrypt("encrypted-account-number")).thenReturn("1234567890123");
        when(sellerRepository.save(org.mockito.ArgumentMatchers.any(Seller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.promoteAfterAccountVerified(memberId, sessionId);

        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(sellerCaptor.capture());
        Seller savedSeller = sellerCaptor.getValue();
        assertEquals(memberId, savedSeller.getMemberId());
        assertEquals("KAKAO", savedSeller.getBankName());
        assertEquals("1234567890123", savedSeller.getAccount());
        assertEquals(MemberRole.SELLER, member.getRole());
        verify(memberEventPublisher).publishSellerPromoted(member, savedSeller);
        verify(sellerDraftStore).deleteDraft(draftId);
        verify(sellerDraftStore).deleteCurrentDraft(memberId);
    }

    @Test
    void promoteAfterAccountVerified_notVerified_throwsException() {
        SellerPromotionService service = new SellerPromotionService(
                sessionStore,
                sellerDraftStore,
                sellerRepository,
                memberRepository,
                accountEncryptionService,
                memberEventPublisher
        );

        UUID memberId = UUID.randomUUID();
        String sessionId = "av_test_session";
        LocalDateTime now = LocalDateTime.now();
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                "ad_test_draft",
                "code-hash",
                now.minusMinutes(1),
                now.plusMinutes(4)
        );

        when(sessionStore.findSession(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
                AccountVerificationNotAllowedException.class,
                () -> service.promoteAfterAccountVerified(memberId, sessionId)
        );

        verify(sellerDraftStore, never()).deleteDraft(org.mockito.ArgumentMatchers.anyString());
        verify(memberEventPublisher, never()).publishSellerPromoted(org.mockito.ArgumentMatchers.any(Member.class), org.mockito.ArgumentMatchers.any(Seller.class));
    }

    private Member createMember(UUID memberId) {
        LocalDateTime now = LocalDateTime.now();
        return Member.create(
                memberId,
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                now,
                now
        );
    }
}
