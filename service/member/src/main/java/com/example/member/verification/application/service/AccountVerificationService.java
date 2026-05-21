package com.example.member.verification.application.service;

import com.example.member.seller.application.service.SellerPromotionService;

import com.example.member.verification.application.dto.command.AccountVerificationConfirmCommand;
import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.auth.application.dto.command.AuthSessionMetadata;
import com.example.member.verification.application.dto.result.AccountVerificationCancelResult;
import com.example.member.verification.application.dto.result.AccountVerificationConfirmResult;
import com.example.member.verification.application.dto.result.AccountVerificationCurrentResult;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.verification.application.port.out.AccountVerificationEventPort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.verification.exception.AccountVerificationAttemptLimitExceededException;
import com.example.member.verification.exception.AccountVerificationNotAllowedException;
import com.example.member.verification.exception.AccountVerificationNotFoundException;
import com.example.member.verification.exception.AccountVerificationResendLimitExceededException;
import com.example.member.verification.exception.ExpiredAccountVerificationException;
import com.example.member.verification.exception.InvalidAccountVerificationCodeException;
import com.example.member.common.config.AccountVerificationProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.verification.domain.enumtype.AccountVerificationStatus;
import com.example.member.seller.infrastructure.crypto.AccountEncryptionService;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSession;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSessionStore;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.seller.infrastructure.redis.seller.SellerDraft;
import com.example.member.seller.infrastructure.redis.seller.SellerDraftStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountVerificationService implements AccountVerificationUsecase {

    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    private final MemberPersistencePort memberPersistencePort;
    private final AccountVerificationSessionStore sessionStore;
    private final SellerDraftStore sellerDraftStore;
    private final AccountEncryptionService accountEncryptionService;
    private final SellerPromotionService sellerPromotionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final AccountVerificationProperties properties;
    private final AccountVerificationEventPort accountVerificationEventPort;

    @Override
    @Transactional
    public AccountVerificationSendResult createAccountVerification(
            UUID memberId,
            AccountVerificationCreateCommand command
    ) {
        Member member = getMember(memberId);
        validateCreateRequest(command);

        cleanupExistingCurrentSession(memberId);

        LocalDateTime now = LocalDateTime.now();
        String draftId = generateDraftId();
        String sessionId = generateSessionId();
        String verificationCode = generateVerificationCode();
        String codeHash = hashCode(verificationCode);
        String normalizedAccount = normalizeAccountNumber(command.accountNumber());
        String maskedAccountNumber = maskAccountNumber(normalizedAccount);
        LocalDateTime expiresAt = now.plus(properties.expiration());

        String encryptedAccountNumber = accountEncryptionService.encrypt(normalizedAccount);
        SellerDraft draft = SellerDraft.create(
                draftId,
                member.getMemberId(),
                sessionId,
                normalizeRequired(command.bankName(), "bankName"),
                encryptedAccountNumber,
                maskedAccountNumber,
                now
        );

        sellerDraftStore.saveDraft(draft, properties.expiration());
        sellerDraftStore.saveCurrentDraft(memberId, draftId, properties.expiration());

        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                member.getMemberId(),
                draftId,
                codeHash,
                now,
                expiresAt
        );

        try {
            sessionStore.saveSession(session, properties.expiration());
            sessionStore.saveCurrentSession(memberId, sessionId, properties.expiration());
        } catch (RuntimeException exception) {
            sellerDraftStore.deleteDraft(draftId);
            sellerDraftStore.deleteCurrentDraft(memberId);
            throw exception;
        }

        return new AccountVerificationSendResult(
                sessionId,
                session.getStatus().name(),
                draft.getAccountNumberMasked(),
                verificationCode,
                expiresAt,
                session.getAttemptCount(),
                session.getResendCount()
        );
    }

    @Override
    @Transactional
    public AccountVerificationConfirmResult confirmAccountVerification(
            UUID memberId,
            UUID authSessionId,
            String sessionId,
            AccountVerificationConfirmCommand command
    ) {
        validateConfirmRequest(sessionId, command);
        acquireLockOrThrow(sessionId);
        try {
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();

            if (session.isExpired(now)) {
                expireSession(session, "SESSION_EXPIRED");
                sessionStore.saveSession(session, Duration.ZERO);
                throw new ExpiredAccountVerificationException();
            }

            if (session.isVerified()) {
                sellerPromotionService.promoteAfterAccountVerified(memberId, sessionId);
                return buildConfirmResponse(session, issuePromotedTokens(memberId, authSessionId));
            }

            if (!session.canConfirm()) {
                throw new AccountVerificationNotAllowedException("계좌 인증 세션은 확인 처리할 수 없습니다.");
            }

            String normalizedCode = normalizeRequired(command.code(), "code");
            if (!session.getCodeHash().equals(hashCode(normalizedCode))) {
                session.markFailed("계좌 인증 코드가 일치하지 않습니다.");
                if (session.getAttemptCount() >= properties.maxAttempts()) {
                    session.markExpired("ATTEMPT_LIMIT_EXCEEDED");
                    sessionStore.saveSession(session, Duration.ZERO);
                    accountVerificationEventPort.publishAccountVerificationFailed(
                            memberId,
                            session.getSessionId(),
                            "ATTEMPT_LIMIT_EXCEEDED"
                    );
                    throw new AccountVerificationAttemptLimitExceededException();
                }
                sessionStore.saveSession(session, properties.expiration());
                throw new InvalidAccountVerificationCodeException();
            }

            session.markVerified(now);
            sessionStore.saveSession(session, properties.expiration());
            sellerPromotionService.promoteAfterAccountVerified(memberId, sessionId);
            return buildConfirmResponse(session, issuePromotedTokens(memberId, authSessionId));
        } finally {
            sessionStore.releaseLock(sessionId);
        }
    }

    @Override
    public AccountVerificationCurrentResult getCurrentAccountVerification(UUID memberId) {
        getMember(memberId);
        Optional<String> currentSessionId = sessionStore.findCurrentSessionId(memberId);
        if (currentSessionId.isEmpty()) {
            return emptyCurrentResponse();
        }

        Optional<AccountVerificationSession> session = sessionStore.findSession(currentSessionId.get());
        if (session.isEmpty()) {
            sessionStore.deleteCurrentSession(memberId);
            sellerDraftStore.deleteCurrentDraft(memberId);
            return emptyCurrentResponse();
        }

        AccountVerificationSession current = session.get();
        if (current.isExpired(LocalDateTime.now()) && current.getStatus() == AccountVerificationStatus.PENDING) {
            expireSession(current, "SESSION_EXPIRED");
            sessionStore.saveSession(current, Duration.ZERO);
        }

        return buildCurrentResponse(current);
    }

    @Override
    @Transactional
    public AccountVerificationSendResult resendAccountVerification(UUID memberId, String sessionId) {
        acquireLockOrThrow(sessionId);
        try {
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();

            if (session.isExpired(now)) {
                expireSession(session, "SESSION_EXPIRED");
                sessionStore.saveSession(session, Duration.ZERO);
                throw new ExpiredAccountVerificationException();
            }
            if (!session.canResend()) {
                throw new AccountVerificationNotAllowedException("계좌 인증 세션은 재전송할 수 없습니다.");
            }
            if (session.getResendCount() >= properties.maxResendCount()) {
                throw new AccountVerificationResendLimitExceededException();
            }

            SellerDraft draft = getDraftBySession(session);
            String verificationCode = generateVerificationCode();
            String codeHash = hashCode(verificationCode);
            LocalDateTime expiresAt = now.plus(properties.expiration());
            session.resend(codeHash, now, expiresAt);
            sessionStore.saveSession(session, properties.expiration());
            sessionStore.saveCurrentSession(memberId, sessionId, properties.expiration());

            return new AccountVerificationSendResult(
                    session.getSessionId(),
                    session.getStatus().name(),
                    draft == null ? null : draft.getAccountNumberMasked(),
                    verificationCode,
                    session.getExpiresAt(),
                    session.getAttemptCount(),
                    session.getResendCount()
            );
        } finally {
            sessionStore.releaseLock(sessionId);
        }
    }

    @Override
    @Transactional
    public AccountVerificationCancelResult cancelAccountVerification(UUID memberId, String sessionId) {
        acquireLockOrThrow(sessionId);
        try {
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();
            session.markCancelled(now);
            sessionStore.saveSession(session, properties.expiration());
            sellerDraftStore.deleteDraft(session.getDraftId());
            sellerDraftStore.deleteCurrentDraft(memberId);
            return new AccountVerificationCancelResult(
                    session.getSessionId(),
                    session.getStatus().name(),
                    session.getCancelledAt()
            );
        } finally {
            sessionStore.releaseLock(sessionId);
        }
    }

    private void cleanupExistingCurrentSession(UUID memberId) {
        Optional<String> currentSessionId = sessionStore.findCurrentSessionId(memberId);
        if (currentSessionId.isPresent()) {
            sessionStore.findSession(currentSessionId.get()).ifPresent(session ->
                    sellerDraftStore.deleteDraft(session.getDraftId())
            );
            sessionStore.deleteCurrentSession(memberId);
            sessionStore.deleteSession(currentSessionId.get());
        }

        Optional<String> currentDraftId = sellerDraftStore.findCurrentDraftId(memberId);
        currentDraftId.ifPresent(sellerDraftStore::deleteDraft);
        sellerDraftStore.deleteCurrentDraft(memberId);
    }

    private AccountVerificationSession getOwnedSession(UUID memberId, String sessionId) {
        AccountVerificationSession session = sessionStore.findSession(sessionId)
                .orElseThrow(AccountVerificationNotFoundException::new);
        if (!session.belongsTo(memberId)) {
            throw new AccountVerificationNotAllowedException("계좌 인증 세션이 현재 회원에게 속하지 않습니다.");
        }
        return session;
    }

    private Member getMember(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    private AccountVerificationCurrentResult emptyCurrentResponse() {
        return new AccountVerificationCurrentResult(
                null,
                AccountVerificationStatus.NONE.name(),
                null,
                null,
                null,
                0,
                0
        );
    }

    private AccountVerificationCurrentResult buildCurrentResponse(AccountVerificationSession session) {
        Optional<SellerDraft> draft = getDraftBySessionOptional(session);
        return new AccountVerificationCurrentResult(
                session.getSessionId(),
                session.getStatus().name(),
                draft.map(SellerDraft::getBankName).orElse(null),
                draft.map(SellerDraft::getAccountNumberMasked).orElse(null),
                session.getExpiresAt(),
                session.getAttemptCount(),
                session.getResendCount()
        );
    }

    private AccountVerificationConfirmResult buildConfirmResponse(
            AccountVerificationSession session,
            AccountVerificationConfirmResult.AuthTokens authTokens
    ) {
        return new AccountVerificationConfirmResult(
                session.getSessionId(),
                true,
                session.getStatus().name(),
                session.getVerifiedAt(),
                session.getAttemptCount(),
                authTokens
        );
    }

    private AccountVerificationConfirmResult.AuthTokens issuePromotedTokens(UUID memberId, UUID authSessionId) {
        Member member = getMember(memberId);
        String accessToken = jwtTokenProvider.createAccessToken(member, authSessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(member, authSessionId);
        ParsedRefreshToken parsedRefreshToken = jwtTokenProvider.parseRefreshToken(refreshToken);
        Duration refreshTtl = Duration.ofMillis(jwtTokenProvider.getRefreshExpiration());

        if (refreshTokenStore.findBySessionId(authSessionId).isPresent()) {
            refreshTokenStore.updateRefreshTokenId(
                    authSessionId,
                    parsedRefreshToken.refreshTokenId(),
                    refreshTtl,
                    AuthSessionMetadata.empty()
            );
        } else {
            refreshTokenStore.createSession(
                    memberId,
                    authSessionId,
                    parsedRefreshToken.refreshTokenId(),
                    refreshTtl,
                    AuthSessionMetadata.empty()
            );
        }

        return new AccountVerificationConfirmResult.AuthTokens(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpiration(),
                jwtTokenProvider.getRefreshExpiration(),
                authSessionId
        );
    }

    private Optional<SellerDraft> getDraftBySessionOptional(AccountVerificationSession session) {
        return sellerDraftStore.findDraft(session.getDraftId());
    }

    private SellerDraft getDraftBySession(AccountVerificationSession session) {
        return sellerDraftStore.findDraft(session.getDraftId()).orElse(null);
    }

    private void validateCreateRequest(AccountVerificationCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("계좌 인증 요청 본문은 필수입니다.");
        }
    }

    private void validateConfirmRequest(String sessionId, AccountVerificationConfirmCommand command) {
        normalizeRequired(sessionId, "sessionId");
        if (command == null) {
            throw new IllegalArgumentException("확인 요청 본문은 필수입니다.");
        }
    }

    private String generateSessionId() {
        return "av_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateDraftId() {
        return "ad_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateVerificationCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private String hashCode(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }

    private void acquireLockOrThrow(String sessionId) {
        if (!sessionStore.acquireLock(sessionId, LOCK_TTL)) {
            throw new AccountVerificationNotAllowedException("다른 계좌 인증 요청이 이미 진행 중입니다.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private String normalizeAccountNumber(String accountNumber) {
        String normalized = normalizeRequired(accountNumber, "accountNumber").replace("-", "").replace(" ", "");
        if (!normalized.matches("\\d{6,20}")) {
            throw new IllegalArgumentException("accountNumber는 숫자만 포함해야 하고 6자 이상 20자 이하여야 합니다.");
        }
        return normalized;
    }

    private String maskAccountNumber(String normalizedAccountNumber) {
        int length = normalizedAccountNumber.length();
        if (length <= 4) {
            return "****";
        }
        if (length <= 8) {
            return normalizedAccountNumber.substring(0, 2) + "****" + normalizedAccountNumber.substring(length - 2);
        }
        return normalizedAccountNumber.substring(0, 3) + "-****-" + normalizedAccountNumber.substring(length - 4);
    }

    private void expireSession(AccountVerificationSession session, String reason) {
        if (session.getStatus() == AccountVerificationStatus.EXPIRED) {
            return;
        }

        session.markExpired(reason);
        accountVerificationEventPort.publishAccountVerificationExpired(
                session.getMemberId(),
                session.getSessionId(),
                reason
        );
    }
}
