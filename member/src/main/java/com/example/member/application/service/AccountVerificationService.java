package com.example.member.application.service;

import com.example.member.application.usecase.AccountVerificationUsecase;
import com.example.member.common.exception.AccountVerificationAttemptLimitExceededException;
import com.example.member.common.exception.AccountVerificationNotAllowedException;
import com.example.member.common.exception.AccountVerificationNotFoundException;
import com.example.member.common.exception.AccountVerificationResendLimitExceededException;
import com.example.member.common.exception.ExpiredAccountVerificationException;
import com.example.member.common.exception.InvalidAccountVerificationCodeException;
import com.example.member.config.AccountVerificationProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.AccountVerificationStatus;
import com.example.member.infrastructure.redis.AccountVerificationSession;
import com.example.member.infrastructure.redis.AccountVerificationSessionStore;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.AccountVerificationCancelResponse;
import com.example.member.presentation.dto.AccountVerificationConfirmRequest;
import com.example.member.presentation.dto.AccountVerificationConfirmResponse;
import com.example.member.presentation.dto.AccountVerificationCreateRequest;
import com.example.member.presentation.dto.AccountVerificationCurrentResponse;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
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

    private final MemberRepository memberRepository;
    private final AccountVerificationSessionStore sessionStore;
    private final AccountVerificationProperties properties;

    @Override
    @Transactional
    public AccountVerificationSendResponse createAccountVerification(UUID memberId, AccountVerificationCreateRequest request) {
        Member member = getMember(memberId);
        validateCreateRequest(request);

        cleanupExistingCurrentSession(memberId);

        LocalDateTime now = LocalDateTime.now();
        String sessionId = generateSessionId();
        String verificationCode = generateVerificationCode();
        String codeHash = hashCode(verificationCode);
        String normalizedAccount = normalizeAccountNumber(request.accountNumber());
        String maskedAccountNumber = maskAccountNumber(normalizedAccount);
        LocalDateTime expiresAt = now.plus(properties.expiration());

        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                member.getMemberId(),
                normalizeRequired(request.bankName(), "bankName"),
                maskedAccountNumber,
                codeHash,
                now,
                expiresAt
        );

        sessionStore.saveSession(session, properties.expiration());
        sessionStore.saveCurrentSession(memberId, sessionId, properties.expiration());

        return new AccountVerificationSendResponse(
                sessionId,
                session.getStatus().name(),
                maskedAccountNumber,
                verificationCode,
                expiresAt,
                session.getAttemptCount(),
                session.getResendCount()
        );
    }

    @Override
    @Transactional
    public AccountVerificationConfirmResponse confirmAccountVerification(
            UUID memberId,
            String sessionId,
            AccountVerificationConfirmRequest request
    ) {
        validateConfirmRequest(sessionId, request);
        acquireLockOrThrow(sessionId);
        try {
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();

            if (session.isExpired(now)) {
                session.markExpired("Account verification session has expired.");
                sessionStore.saveSession(session, Duration.ZERO);
                throw new ExpiredAccountVerificationException();
            }

            if (!session.canConfirm()) {
                throw new AccountVerificationNotAllowedException("Account verification session cannot be confirmed.");
            }

            String normalizedCode = normalizeRequired(request.code(), "code");
            if (!session.getCodeHash().equals(hashCode(normalizedCode))) {
                session.markFailed("Account verification code mismatch.");
                if (session.getAttemptCount() >= properties.maxAttempts()) {
                    session.markExpired("Account verification attempt limit exceeded.");
                    sessionStore.saveSession(session, Duration.ZERO);
                    throw new AccountVerificationAttemptLimitExceededException();
                }
                sessionStore.saveSession(session, properties.expiration());
                throw new InvalidAccountVerificationCodeException();
            }

            session.markVerified(now);
            sessionStore.saveSession(session, properties.expiration());
            return new AccountVerificationConfirmResponse(
                    session.getSessionId(),
                    true,
                    session.getStatus().name(),
                    session.getVerifiedAt(),
                    session.getAttemptCount()
            );
        } finally {
            sessionStore.releaseLock(sessionId);
        }
    }

    @Override
    public AccountVerificationCurrentResponse getCurrentAccountVerification(UUID memberId) {
        getMember(memberId);
        Optional<String> currentSessionId = sessionStore.findCurrentSessionId(memberId);
        if (currentSessionId.isEmpty()) {
            return emptyCurrentResponse();
        }

        Optional<AccountVerificationSession> session = sessionStore.findSession(currentSessionId.get());
        if (session.isEmpty()) {
            sessionStore.deleteCurrentSession(memberId);
            return emptyCurrentResponse();
        }

        AccountVerificationSession current = session.get();
        if (current.isExpired(LocalDateTime.now()) && current.getStatus() == AccountVerificationStatus.PENDING) {
            current.markExpired("Account verification session has expired.");
            sessionStore.saveSession(current, Duration.ZERO);
        }

        return new AccountVerificationCurrentResponse(
                current.getSessionId(),
                current.getStatus().name(),
                current.getBankName(),
                current.getAccountNumberMasked(),
                current.getExpiresAt(),
                current.getAttemptCount(),
                current.getResendCount()
        );
    }

    @Override
    @Transactional
    public AccountVerificationSendResponse resendAccountVerification(UUID memberId, String sessionId) {
        acquireLockOrThrow(sessionId);
        try {
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();

            if (session.isExpired(now)) {
                session.markExpired("Account verification session has expired.");
                sessionStore.saveSession(session, Duration.ZERO);
                throw new ExpiredAccountVerificationException();
            }
            if (!session.canResend()) {
                throw new AccountVerificationNotAllowedException("Account verification session cannot be resent.");
            }
            if (session.getResendCount() >= properties.maxResendCount()) {
                throw new AccountVerificationResendLimitExceededException();
            }

            String verificationCode = generateVerificationCode();
            String codeHash = hashCode(verificationCode);
            LocalDateTime expiresAt = now.plus(properties.expiration());
            session.resend(codeHash, now, expiresAt);
            sessionStore.saveSession(session, properties.expiration());
            sessionStore.saveCurrentSession(memberId, sessionId, properties.expiration());

            return new AccountVerificationSendResponse(
                    session.getSessionId(),
                    session.getStatus().name(),
                    session.getAccountNumberMasked(),
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
    public AccountVerificationCancelResponse cancelAccountVerification(UUID memberId, String sessionId) {
        acquireLockOrThrow(sessionId);
        try {
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();
            session.markCancelled(now);
            sessionStore.saveSession(session, properties.expiration());
            return new AccountVerificationCancelResponse(
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
        if (currentSessionId.isEmpty()) {
            return;
        }

        sessionStore.deleteCurrentSession(memberId);
        sessionStore.deleteSession(currentSessionId.get());
    }

    private AccountVerificationSession getOwnedSession(UUID memberId, String sessionId) {
        AccountVerificationSession session = sessionStore.findSession(sessionId)
                .orElseThrow(AccountVerificationNotFoundException::new);
        if (!session.belongsTo(memberId)) {
            throw new AccountVerificationNotAllowedException("Account verification session does not belong to current member.");
        }
        return session;
    }

    private Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member was not found."));
    }

    private AccountVerificationCurrentResponse emptyCurrentResponse() {
        return new AccountVerificationCurrentResponse(
                null,
                AccountVerificationStatus.NONE.name(),
                null,
                null,
                null,
                0,
                0
        );
    }

    private void validateCreateRequest(AccountVerificationCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Account verification request body is required.");
        }
    }

    private void validateConfirmRequest(String sessionId, AccountVerificationConfirmRequest request) {
        normalizeRequired(sessionId, "sessionId");
        if (request == null) {
            throw new IllegalArgumentException("Confirm request body is required.");
        }
    }

    private String generateSessionId() {
        return "av_" + UUID.randomUUID().toString().replace("-", "");
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
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }

    private void acquireLockOrThrow(String sessionId) {
        if (!sessionStore.acquireLock(sessionId, LOCK_TTL)) {
            throw new AccountVerificationNotAllowedException("Another account verification request is already in progress.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private String normalizeAccountNumber(String accountNumber) {
        String normalized = normalizeRequired(accountNumber, "accountNumber").replace("-", "").replace(" ", "");
        if (!normalized.matches("\\d{6,20}")) {
            throw new IllegalArgumentException("accountNumber must contain only digits and be between 6 and 20 characters.");
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
}
