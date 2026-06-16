package com.example.member.verification.application.service;


import com.example.member.common.exception.BusinessException;
import com.example.member.verification.exception.VerificationErrorCode;
import com.example.member.seller.application.service.SellerPromotionService;

import com.example.member.verification.application.dto.command.AccountVerificationConfirmCommand;
import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.verification.application.dto.result.AccountVerificationCancelResult;
import com.example.member.verification.application.dto.result.AccountVerificationConfirmResult;
import com.example.member.verification.application.dto.result.AccountVerificationCurrentResult;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.verification.application.port.out.AccountVerificationEventPort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.verification.config.AccountVerificationProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.verification.domain.enumtype.AccountVerificationStatus;
import com.example.member.seller.infrastructure.crypto.AccountEncryptionService;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSession;
import com.example.member.verification.infrastructure.redis.accountverification.AccountVerificationSessionStore;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
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
import org.springframework.validation.annotation.Validated;

@Service
@Validated
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
        // [1] 회원 존재 확인
        Member member = getMember(memberId);

        // [2] 기존 계좌 인증 세션 및 판매자 draft 정리
        cleanupExistingCurrentSession(memberId);

        // [3] 인증 세션 기본값 생성
        LocalDateTime now = LocalDateTime.now();
        String draftId = generateDraftId();
        String sessionId = generateSessionId();
        String verificationCode = generateVerificationCode();
        String codeHash = hashCode(verificationCode);
        String normalizedAccount = normalizeAccountNumber(command.accountNumber());
        String maskedAccountNumber = maskAccountNumber(normalizedAccount);
        LocalDateTime expiresAt = now.plus(properties.expiration());

        // [4] 계좌번호 암호화 및 판매자 draft 생성
        String encryptedAccountNumber = accountEncryptionService.encrypt(normalizedAccount);
        SellerDraft draft = SellerDraft.create(
                draftId,
                member.getMemberId(),
                sessionId,
                command.bankName().trim(),
                encryptedAccountNumber,
                maskedAccountNumber,
                now
        );

        // [5] 판매자 draft Redis 저장
        sellerDraftStore.saveDraft(draft, properties.expiration());
        sellerDraftStore.saveCurrentDraft(memberId, draftId, properties.expiration());

        // [6] 계좌 인증 세션 생성
        AccountVerificationSession session = AccountVerificationSession.create(
                sessionId,
                member.getMemberId(),
                draftId,
                codeHash,
                now,
                expiresAt
        );

        // [7] 인증 세션 저장 실패 시 draft 보상 삭제
        try {
            sessionStore.saveSession(session, properties.expiration());
            sessionStore.saveCurrentSession(memberId, sessionId, properties.expiration());
        } catch (RuntimeException exception) {
            sellerDraftStore.deleteDraft(draftId);
            sellerDraftStore.deleteCurrentDraft(memberId);
            throw exception;
        }

        // [8] 인증 코드 포함 응답 반환
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
        // [1] 세션 ID 검증 및 중복 처리 방지 lock 획득
        validateSessionId(sessionId);
        acquireLockOrThrow(sessionId);
        try {
            // [2] 회원 소유 세션 조회
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();

            // [3] 만료 세션 처리
            if (session.isExpired(now)) {
                expireSession(session, "SESSION_EXPIRED");
                sessionStore.saveSession(session, Duration.ZERO);
                throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_EXPIRED);
            }

            // [4] 이미 인증 완료된 세션이면 판매자 승격 및 토큰 재발급
            if (session.isVerified()) {
                sellerPromotionService.promoteAfterAccountVerified(memberId, sessionId);
                return buildConfirmResponse(session, issuePromotedTokens(memberId, authSessionId));
            }

            // [5] 인증 가능 상태 검증
            if (!session.canConfirm()) {
                throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_NOT_ALLOWED, "계좌 인증 세션을 확인 처리할 수 없습니다.");
            }

            // [6] 인증 코드 불일치 처리 및 시도 횟수 제한 확인
            String normalizedCode = command.code().trim();
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
                    throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_ATTEMPT_LIMIT_EXCEEDED);
                }
                sessionStore.saveSession(session, properties.expiration());
                throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_CODE_INVALID);
            }

            // [7] 인증 완료 처리
            session.markVerified(now);
            sessionStore.saveSession(session, properties.expiration());

            // [8] 판매자 승격 및 신규 토큰 발급
            sellerPromotionService.promoteAfterAccountVerified(memberId, sessionId);
            return buildConfirmResponse(session, issuePromotedTokens(memberId, authSessionId));
        } finally {
            // [9] 세션 lock 해제
            sessionStore.releaseLock(sessionId);
        }
    }

    @Override
    public AccountVerificationCurrentResult getCurrentAccountVerification(UUID memberId) {
        // [1] 회원 존재 확인
        getMember(memberId);

        // [2] 현재 세션 ID 조회
        Optional<String> currentSessionId = sessionStore.findCurrentSessionId(memberId);
        if (currentSessionId.isEmpty()) {
            return emptyCurrentResponse();
        }

        // [3] 세션 본문 조회 실패 시 현재 세션 및 draft 정리
        Optional<AccountVerificationSession> session = sessionStore.findSession(currentSessionId.get());
        if (session.isEmpty()) {
            sessionStore.deleteCurrentSession(memberId);
            sellerDraftStore.deleteCurrentDraft(memberId);
            return emptyCurrentResponse();
        }

        // [4] 만료된 pending 세션 종료 처리
        AccountVerificationSession current = session.get();
        if (current.isExpired(LocalDateTime.now()) && current.getStatus() == AccountVerificationStatus.PENDING) {
            expireSession(current, "SESSION_EXPIRED");
            sessionStore.saveSession(current, Duration.ZERO);
        }

        // [5] 현재 세션 응답 변환
        return buildCurrentResponse(current);
    }

    @Override
    @Transactional
    public AccountVerificationSendResult resendAccountVerification(UUID memberId, String sessionId) {
        // [1] 중복 재발송 방지 lock 획득
        acquireLockOrThrow(sessionId);
        try {
            // [2] 회원 소유 세션 조회
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);
            LocalDateTime now = LocalDateTime.now();

            // [3] 만료 세션 처리
            if (session.isExpired(now)) {
                expireSession(session, "SESSION_EXPIRED");
                sessionStore.saveSession(session, Duration.ZERO);
                throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_EXPIRED);
            }

            // [4] 재발송 가능 상태 및 제한 횟수 검증
            if (!session.canResend()) {
                throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_NOT_ALLOWED, "계좌 인증 세션을 재전송할 수 없습니다.");
            }
            if (session.getResendCount() >= properties.maxResendCount()) {
                throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_RESEND_LIMIT_EXCEEDED);
            }

            // [5] 새 인증 코드 생성 및 세션 갱신
            SellerDraft draft = getDraftBySession(session);
            String verificationCode = generateVerificationCode();
            String codeHash = hashCode(verificationCode);
            LocalDateTime expiresAt = now.plus(properties.expiration());
            session.resend(codeHash, now, expiresAt);
            sessionStore.saveSession(session, properties.expiration());
            sessionStore.saveCurrentSession(memberId, sessionId, properties.expiration());

            // [6] 새 인증 코드 포함 응답 반환
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
            // [7] 세션 lock 해제
            sessionStore.releaseLock(sessionId);
        }
    }

    @Override
    @Transactional
    public AccountVerificationCancelResult cancelAccountVerification(UUID memberId, String sessionId) {
        // [1] 중복 취소 방지 lock 획득
        acquireLockOrThrow(sessionId);
        try {
            // [2] 회원 소유 세션 조회
            AccountVerificationSession session = getOwnedSession(memberId, sessionId);

            // [3] 세션 취소 상태 저장
            LocalDateTime now = LocalDateTime.now();
            session.markCancelled(now);
            sessionStore.saveSession(session, properties.expiration());

            // [4] 판매자 draft 정리
            sellerDraftStore.deleteDraft(session.getDraftId());
            sellerDraftStore.deleteCurrentDraft(memberId);

            // [5] 취소 응답 반환
            return new AccountVerificationCancelResult(
                    session.getSessionId(),
                    session.getStatus().name(),
                    session.getCancelledAt()
            );
        } finally {
            // [6] 세션 lock 해제
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
                .orElseThrow(() -> new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND));
        if (!session.belongsTo(memberId)) {
            throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_NOT_ALLOWED, "계좌 인증 세션이 현재 회원에게 속하지 않습니다.");
        }
        return session;
    }

    private Member getMember(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
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

        AuthSession authSession = refreshTokenStore.findBySessionId(authSessionId)
                .map(session -> session.refresh(
                        parsedRefreshToken.refreshTokenId(),
                        AuthSessionMetadata.empty()
                ))
                .orElseGet(() -> AuthSession.create(
                        memberId,
                        authSessionId,
                        parsedRefreshToken.refreshTokenId(),
                        AuthSessionMetadata.empty()
                ));
        refreshTokenStore.saveSession(authSession, refreshTtl);

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

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // TODO: 내부 호출 경계가 생기면 중복 필수값 검증을 command validation으로 이동
            throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_NOT_FOUND);
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
            throw new BusinessException(VerificationErrorCode.ACCOUNT_VERIFICATION_NOT_ALLOWED, "다른 계좌 인증 요청이 이미 진행 중입니다.");
        }
    }

    private String normalizeAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            // TODO: 내부 호출 경계가 생기면 중복 필수값 검증을 command validation으로 이동
            throw new BusinessException(VerificationErrorCode.INVALID_ACCOUNT_NUMBER);
        }
        String normalized = accountNumber.trim().replace("-", "").replace(" ", "");
        if (!normalized.matches("\\d{6,20}")) {
            throw new BusinessException(VerificationErrorCode.INVALID_ACCOUNT_NUMBER);
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
