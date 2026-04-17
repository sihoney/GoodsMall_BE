package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.exception.InvalidAccountVerificationCodeException;
import com.example.member.config.AccountVerificationProperties;
import com.example.member.infrastructure.crypto.AccountEncryptionService;
import com.example.member.domain.entity.Member;
import com.example.member.infrastructure.redis.AccountVerificationSession;
import com.example.member.infrastructure.redis.AccountVerificationSessionStore;
import com.example.member.infrastructure.redis.SellerDraft;
import com.example.member.infrastructure.redis.SellerDraftStore;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.AccountVerificationConfirmRequest;
import com.example.member.presentation.dto.AccountVerificationCreateRequest;
import com.example.member.presentation.dto.AccountVerificationConfirmResponse;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountVerificationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AccountVerificationSessionStore sessionStore;

    @Mock
    private SellerDraftStore sellerDraftStore;

    @Mock
    private AccountEncryptionService accountEncryptionService;

    @Mock
    private SellerPromotionService sellerPromotionService;

    private final AccountVerificationProperties properties = new AccountVerificationProperties(
            Duration.ofMinutes(5),
            5,
            3,
            Duration.ofSeconds(30)
    );

    @Test
    void createAccountVerification_success_savesPendingSession() {
        AccountVerificationService service = new AccountVerificationService(
                memberRepository,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                properties
        );
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        AccountVerificationCreateRequest request = new AccountVerificationCreateRequest("KAKAO", "1234567890123");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(sessionStore.findCurrentSessionId(memberId)).thenReturn(Optional.empty());
        when(sellerDraftStore.findCurrentDraftId(memberId)).thenReturn(Optional.empty());
        when(accountEncryptionService.encrypt("1234567890123")).thenReturn("encrypted-account-number");

        AccountVerificationSendResponse response = service.createAccountVerification(memberId, request);

        ArgumentCaptor<SellerDraft> draftCaptor = ArgumentCaptor.forClass(SellerDraft.class);
        ArgumentCaptor<AccountVerificationSession> sessionCaptor = ArgumentCaptor.forClass(AccountVerificationSession.class);
        verify(sellerDraftStore).saveDraft(draftCaptor.capture(), any(Duration.class));
        verify(sellerDraftStore).saveCurrentDraft(eq(memberId), any(String.class), any(Duration.class));
        verify(sessionStore).saveSession(sessionCaptor.capture(), any(Duration.class));
        verify(sessionStore).saveCurrentSession(any(UUID.class), any(String.class), any(Duration.class));

        SellerDraft savedDraft = draftCaptor.getValue();
        AccountVerificationSession savedSession = sessionCaptor.getValue();
        assertEquals(memberId, savedDraft.getMemberId());
        assertEquals("KAKAO", savedDraft.getBankName());
        assertEquals("encrypted-account-number", savedDraft.getEncryptedAccountNumber());
        assertEquals("123-****-0123", savedDraft.getAccountNumberMasked());
        assertEquals(memberId, savedSession.getMemberId());
        assertEquals(savedDraft.getDraftId(), savedSession.getDraftId());
        assertEquals(memberId, savedSession.getMemberId());
        assertEquals("PENDING", savedSession.getStatus().name());
        assertEquals(6, response.verificationCode().length());
        assertNotNull(response.sessionId());
        assertEquals("PENDING", response.status());
        assertEquals("123-****-0123", response.maskedAccountNumber());
    }

    @Test
    void confirmAccountVerification_invalidCode_throwsException() {
        AccountVerificationService service = new AccountVerificationService(
                memberRepository,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                properties
        );
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        String sessionId = "av_test_session";
        String correctCode = "482931";
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                "ad_test_draft",
                hash(correctCode),
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(4)
        );

        when(sessionStore.acquireLock(sessionId, Duration.ofSeconds(5))).thenReturn(true);
        when(sessionStore.findSession(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
                InvalidAccountVerificationCodeException.class,
                () -> service.confirmAccountVerification(memberId, sessionId, new AccountVerificationConfirmRequest("111111"))
        );

        verify(sessionStore).saveSession(any(AccountVerificationSession.class), any(Duration.class));
        verify(sessionStore).releaseLock(sessionId);
    }

    @Test
    void confirmAccountVerification_success_returnsVerifiedResponse() {
        AccountVerificationService service = new AccountVerificationService(
                memberRepository,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                properties
        );
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        String sessionId = "av_test_session";
        String correctCode = "482931";
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                "ad_test_draft",
                hash(correctCode),
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(4)
        );

        when(sessionStore.acquireLock(sessionId, Duration.ofSeconds(5))).thenReturn(true);
        when(sessionStore.findSession(sessionId)).thenReturn(Optional.of(session));

        AccountVerificationConfirmResponse response = service.confirmAccountVerification(
                memberId,
                sessionId,
                new AccountVerificationConfirmRequest(correctCode)
        );

        assertEquals(sessionId, response.sessionId());
        assertEquals(true, response.verified());
        assertEquals("VERIFIED", response.status());
        assertNotNull(response.verifiedAt());
        verify(sessionStore).saveSession(any(AccountVerificationSession.class), any(Duration.class));
        verify(sellerPromotionService).promoteAfterAccountVerified(memberId, sessionId);
        verify(sessionStore).releaseLock(sessionId);
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
                com.todaylunch.common.security.auth.enumtype.MemberRole.USER,
                com.example.member.domain.enumtype.MemberStatus.ACTIVE,
                now,
                now
        );
    }

    private String hash(String value) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hashed);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
