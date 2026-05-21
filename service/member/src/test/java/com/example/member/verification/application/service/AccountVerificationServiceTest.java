package com.example.member.verification.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.verification.application.dto.command.AccountVerificationConfirmCommand;
import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.auth.application.dto.command.AuthSessionMetadata;
import com.example.member.verification.application.dto.result.AccountVerificationConfirmResult;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import com.example.member.verification.application.port.out.AccountVerificationEventPort;
import com.example.member.verification.exception.AccountVerificationAttemptLimitExceededException;
import com.example.member.verification.exception.ExpiredAccountVerificationException;
import com.example.member.verification.exception.InvalidAccountVerificationCodeException;
import com.example.member.common.config.AccountVerificationProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.seller.application.service.SellerPromotionService;
import com.example.member.seller.infrastructure.crypto.AccountEncryptionService;
import com.example.member.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSession;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSessionStore;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.seller.infrastructure.redis.seller.SellerDraft;
import com.example.member.seller.infrastructure.redis.seller.SellerDraftStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
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
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private AccountVerificationSessionStore sessionStore;

    @Mock
    private SellerDraftStore sellerDraftStore;

    @Mock
    private AccountEncryptionService accountEncryptionService;

    @Mock
    private SellerPromotionService sellerPromotionService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private AccountVerificationEventPort memberEventPort;

    private final AccountVerificationProperties properties = new AccountVerificationProperties(
            Duration.ofMinutes(5),
            5,
            3,
            Duration.ofSeconds(30)
    );

    @Test
    void createAccountVerification_success_savesPendingSession() {
        AccountVerificationService service = new AccountVerificationService(
                memberPersistencePort,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                jwtTokenProvider,
                refreshTokenStore,
                properties,
                memberEventPort
        );
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        AccountVerificationCreateCommand command = new AccountVerificationCreateCommand("KAKAO", "1234567890123");

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(sessionStore.findCurrentSessionId(memberId)).thenReturn(Optional.empty());
        when(sellerDraftStore.findCurrentDraftId(memberId)).thenReturn(Optional.empty());
        when(accountEncryptionService.encrypt("1234567890123")).thenReturn("encrypted-account-number");

        AccountVerificationSendResult response = service.createAccountVerification(memberId, command);

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
        assertEquals("PENDING", savedSession.getStatus().name());
        assertEquals(6, response.verificationCode().length());
        assertNotNull(response.sessionId());
        assertEquals("PENDING", response.status());
        assertEquals("123-****-0123", response.maskedAccountNumber());
    }

    @Test
    void confirmAccountVerification_invalidCode_throwsException() {
        AccountVerificationService service = new AccountVerificationService(
                memberPersistencePort,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                jwtTokenProvider,
                refreshTokenStore,
                properties,
                memberEventPort
        );
        UUID memberId = UUID.randomUUID();
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
                () -> service.confirmAccountVerification(
                        memberId,
                        UUID.randomUUID(),
                        sessionId,
                        new AccountVerificationConfirmCommand("111111")
                )
        );

        verify(sessionStore).saveSession(any(AccountVerificationSession.class), any(Duration.class));
        verify(sessionStore).releaseLock(sessionId);
        verify(memberEventPort, never()).publishAccountVerificationExpired(any(UUID.class), anyString(), anyString());
        verify(memberEventPort, never()).publishAccountVerificationFailed(any(UUID.class), anyString(), anyString());
    }

    @Test
    void confirmAccountVerification_success_returnsVerifiedResponseWithRotatedTokens() {
        AccountVerificationService service = new AccountVerificationService(
                memberPersistencePort,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                jwtTokenProvider,
                refreshTokenStore,
                properties,
                memberEventPort
        );
        UUID memberId = UUID.randomUUID();
        UUID authSessionId = UUID.randomUUID();
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
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtTokenProvider.createAccessToken(member, authSessionId)).thenReturn("seller-access-token");
        when(jwtTokenProvider.createRefreshToken(member, authSessionId)).thenReturn("seller-refresh-token");
        when(jwtTokenProvider.parseRefreshToken("seller-refresh-token"))
                .thenReturn(new ParsedRefreshToken(memberId, authSessionId, "seller-refresh-token-id"));
        when(jwtTokenProvider.getAccessExpiration()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);
        when(refreshTokenStore.findBySessionId(authSessionId)).thenReturn(Optional.empty());

        AccountVerificationConfirmResult response = service.confirmAccountVerification(
                memberId,
                authSessionId,
                sessionId,
                new AccountVerificationConfirmCommand(correctCode)
        );

        assertEquals(sessionId, response.sessionId());
        assertEquals(true, response.verified());
        assertEquals("VERIFIED", response.status());
        assertNotNull(response.verifiedAt());
        assertNotNull(response.auth());
        assertEquals("seller-access-token", response.auth().accessToken());
        assertEquals("seller-refresh-token", response.auth().refreshToken());
        assertEquals("Bearer", response.auth().tokenType());
        assertEquals(authSessionId, response.auth().sessionId());
        verify(sessionStore).saveSession(any(AccountVerificationSession.class), any(Duration.class));
        verify(sellerPromotionService).promoteAfterAccountVerified(memberId, sessionId);
        verify(refreshTokenStore).createSession(
                memberId,
                authSessionId,
                "seller-refresh-token-id",
                Duration.ofMillis(7200L),
                AuthSessionMetadata.empty()
        );
        verify(sessionStore).releaseLock(sessionId);
        verify(memberEventPort, never()).publishAccountVerificationExpired(any(UUID.class), anyString(), anyString());
        verify(memberEventPort, never()).publishAccountVerificationFailed(any(UUID.class), anyString(), anyString());
    }

    @Test
    void confirmAccountVerification_expired_publishesExpiredEvent() {
        AccountVerificationService service = new AccountVerificationService(
                memberPersistencePort,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                jwtTokenProvider,
                refreshTokenStore,
                properties,
                memberEventPort
        );
        UUID memberId = UUID.randomUUID();
        String sessionId = "av_expired_session";
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                memberId,
                "ad_test_draft",
                hash("482931"),
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now().minusMinutes(1)
        );

        when(sessionStore.acquireLock(sessionId, Duration.ofSeconds(5))).thenReturn(true);
        when(sessionStore.findSession(sessionId)).thenReturn(Optional.of(session));

        assertThrows(
                ExpiredAccountVerificationException.class,
                () -> service.confirmAccountVerification(
                        memberId,
                        UUID.randomUUID(),
                        sessionId,
                        new AccountVerificationConfirmCommand("482931")
                )
        );

        verify(sessionStore).saveSession(any(AccountVerificationSession.class), eq(Duration.ZERO));
        verify(memberEventPort).publishAccountVerificationExpired(memberId, sessionId, "SESSION_EXPIRED");
        verify(memberEventPort, never()).publishAccountVerificationFailed(any(UUID.class), anyString(), anyString());
        verify(sessionStore).releaseLock(sessionId);
    }

    @Test
    void confirmAccountVerification_attemptLimitExceeded_publishesFailedEvent() {
        AccountVerificationProperties strictProperties = new AccountVerificationProperties(
                Duration.ofMinutes(5),
                1,
                3,
                Duration.ofSeconds(30)
        );
        AccountVerificationService service = new AccountVerificationService(
                memberPersistencePort,
                sessionStore,
                sellerDraftStore,
                accountEncryptionService,
                sellerPromotionService,
                jwtTokenProvider,
                refreshTokenStore,
                strictProperties,
                memberEventPort
        );
        UUID memberId = UUID.randomUUID();
        String sessionId = "av_failed_session";
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
                AccountVerificationAttemptLimitExceededException.class,
                () -> service.confirmAccountVerification(
                        memberId,
                        UUID.randomUUID(),
                        sessionId,
                        new AccountVerificationConfirmCommand("111111")
                )
        );

        verify(sessionStore).saveSession(any(AccountVerificationSession.class), eq(Duration.ZERO));
        verify(memberEventPort).publishAccountVerificationFailed(memberId, sessionId, "ATTEMPT_LIMIT_EXCEEDED");
        verify(memberEventPort, never()).publishAccountVerificationExpired(any(UUID.class), anyString(), anyString());
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
                com.example.member.member.domain.enumtype.MemberStatus.ACTIVE,
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
