package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.exception.InvalidAccountVerificationCodeException;
import com.example.member.config.AccountVerificationProperties;
import com.example.member.domain.entity.Member;
import com.example.member.infrastructure.redis.AccountVerificationSession;
import com.example.member.infrastructure.redis.AccountVerificationSessionStore;
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

    private final AccountVerificationProperties properties = new AccountVerificationProperties(
            Duration.ofMinutes(5),
            5,
            3,
            Duration.ofSeconds(30)
    );

    @Test
    void createAccountVerification_success_savesPendingSession() {
        AccountVerificationService service = new AccountVerificationService(memberRepository, sessionStore, properties);
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        AccountVerificationCreateRequest request = new AccountVerificationCreateRequest("KAKAO", "1234567890123");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(sessionStore.findCurrentSessionId(memberId)).thenReturn(Optional.empty());

        AccountVerificationSendResponse response = service.createAccountVerification(memberId, request);

        ArgumentCaptor<AccountVerificationSession> sessionCaptor = ArgumentCaptor.forClass(AccountVerificationSession.class);
        verify(sessionStore).saveSession(sessionCaptor.capture(), any(Duration.class));
        verify(sessionStore).saveCurrentSession(any(UUID.class), any(String.class), any(Duration.class));

        AccountVerificationSession savedSession = sessionCaptor.getValue();
        assertEquals(memberId, savedSession.getMemberId());
        assertEquals("KAKAO", savedSession.getBankName());
        assertEquals("123-****-0123", savedSession.getAccountNumberMasked());
        assertEquals("PENDING", savedSession.getStatus().name());
        assertEquals(6, response.verificationCode().length());
        assertNotNull(response.sessionId());
        assertEquals("PENDING", response.status());
        assertEquals("123-****-0123", response.maskedAccountNumber());
    }

    @Test
    void confirmAccountVerification_invalidCode_throwsException() {
        AccountVerificationService service = new AccountVerificationService(memberRepository, sessionStore, properties);
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        String sessionId = "av_test_session";
        String correctCode = "482931";
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                "KAKAO",
                "123-****-0123",
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
        AccountVerificationService service = new AccountVerificationService(memberRepository, sessionStore, properties);
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        String sessionId = "av_test_session";
        String correctCode = "482931";
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                "KAKAO",
                "123-****-0123",
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
