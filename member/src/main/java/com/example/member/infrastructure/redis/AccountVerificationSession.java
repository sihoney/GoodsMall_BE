package com.example.member.infrastructure.redis;

import com.example.member.domain.enumtype.AccountVerificationStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class AccountVerificationSession {

    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_BANK_NAME = "bankName";
    private static final String FIELD_ACCOUNT_NUMBER_MASKED = "accountNumberMasked";
    private static final String FIELD_CODE_HASH = "codeHash";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_ATTEMPT_COUNT = "attemptCount";
    private static final String FIELD_RESEND_COUNT = "resendCount";
    private static final String FIELD_REQUESTED_AT = "requestedAt";
    private static final String FIELD_EXPIRES_AT = "expiresAt";
    private static final String FIELD_VERIFIED_AT = "verifiedAt";
    private static final String FIELD_CANCELLED_AT = "cancelledAt";
    private static final String FIELD_FAILURE_REASON = "failureReason";

    private final String sessionId;
    private final UUID memberId;
    private final String bankName;
    private final String accountNumberMasked;
    private String codeHash;
    private AccountVerificationStatus status;
    private int attemptCount;
    private int resendCount;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime cancelledAt;
    private String failureReason;

    private AccountVerificationSession(
            String sessionId,
            UUID memberId,
            String bankName,
            String accountNumberMasked,
            String codeHash,
            AccountVerificationStatus status,
            int attemptCount,
            int resendCount,
            LocalDateTime requestedAt,
            LocalDateTime expiresAt,
            LocalDateTime verifiedAt,
            LocalDateTime cancelledAt,
            String failureReason
    ) {
        this.sessionId = Objects.requireNonNull(sessionId);
        this.memberId = Objects.requireNonNull(memberId);
        this.bankName = Objects.requireNonNull(bankName);
        this.accountNumberMasked = Objects.requireNonNull(accountNumberMasked);
        this.codeHash = Objects.requireNonNull(codeHash);
        this.status = Objects.requireNonNull(status);
        this.attemptCount = attemptCount;
        this.resendCount = resendCount;
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.verifiedAt = verifiedAt;
        this.cancelledAt = cancelledAt;
        this.failureReason = failureReason;
    }

    public static AccountVerificationSession create(
            String sessionId,
            UUID memberId,
            String bankName,
            String accountNumberMasked,
            String codeHash,
            LocalDateTime requestedAt,
            LocalDateTime expiresAt
    ) {
        return new AccountVerificationSession(
                sessionId,
                memberId,
                bankName,
                accountNumberMasked,
                codeHash,
                AccountVerificationStatus.PENDING,
                0,
                0,
                requestedAt,
                expiresAt,
                null,
                null,
                null
        );
    }

    public static AccountVerificationSession fromMap(Map<Object, Object> entries) {
        return new AccountVerificationSession(
                stringValue(entries, FIELD_SESSION_ID),
                UUID.fromString(stringValue(entries, FIELD_MEMBER_ID)),
                stringValue(entries, FIELD_BANK_NAME),
                stringValue(entries, FIELD_ACCOUNT_NUMBER_MASKED),
                stringValue(entries, FIELD_CODE_HASH),
                AccountVerificationStatus.valueOf(stringValue(entries, FIELD_STATUS)),
                intValue(entries, FIELD_ATTEMPT_COUNT),
                intValue(entries, FIELD_RESEND_COUNT),
                LocalDateTime.parse(stringValue(entries, FIELD_REQUESTED_AT)),
                LocalDateTime.parse(stringValue(entries, FIELD_EXPIRES_AT)),
                optionalLocalDateTime(entries, FIELD_VERIFIED_AT),
                optionalLocalDateTime(entries, FIELD_CANCELLED_AT),
                optionalString(entries, FIELD_FAILURE_REASON)
        );
    }

    public Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();
        values.put(FIELD_SESSION_ID, sessionId);
        values.put(FIELD_MEMBER_ID, memberId.toString());
        values.put(FIELD_BANK_NAME, bankName);
        values.put(FIELD_ACCOUNT_NUMBER_MASKED, accountNumberMasked);
        values.put(FIELD_CODE_HASH, codeHash);
        values.put(FIELD_STATUS, status.name());
        values.put(FIELD_ATTEMPT_COUNT, Integer.toString(attemptCount));
        values.put(FIELD_RESEND_COUNT, Integer.toString(resendCount));
        values.put(FIELD_REQUESTED_AT, requestedAt.toString());
        values.put(FIELD_EXPIRES_AT, expiresAt.toString());
        values.put(FIELD_VERIFIED_AT, verifiedAt == null ? "" : verifiedAt.toString());
        values.put(FIELD_CANCELLED_AT, cancelledAt == null ? "" : cancelledAt.toString());
        values.put(FIELD_FAILURE_REASON, failureReason == null ? "" : failureReason);
        return values;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean belongsTo(UUID memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean canConfirm() {
        return status == AccountVerificationStatus.PENDING || status == AccountVerificationStatus.FAILED;
    }

    public boolean canResend() {
        return status == AccountVerificationStatus.PENDING || status == AccountVerificationStatus.FAILED;
    }

    public void markVerified(LocalDateTime verifiedAt) {
        this.status = AccountVerificationStatus.VERIFIED;
        this.verifiedAt = Objects.requireNonNull(verifiedAt);
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = AccountVerificationStatus.FAILED;
        this.attemptCount++;
        this.failureReason = reason;
    }

    public void markExpired(String reason) {
        this.status = AccountVerificationStatus.EXPIRED;
        this.failureReason = reason;
    }

    public void markCancelled(LocalDateTime cancelledAt) {
        this.status = AccountVerificationStatus.CANCELLED;
        this.cancelledAt = Objects.requireNonNull(cancelledAt);
        this.failureReason = null;
    }

    public void resend(String newCodeHash, LocalDateTime now, LocalDateTime expiresAt) {
        this.codeHash = Objects.requireNonNull(newCodeHash);
        this.status = AccountVerificationStatus.PENDING;
        this.requestedAt = Objects.requireNonNull(now);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.attemptCount = 0;
        this.resendCount++;
        this.verifiedAt = null;
        this.cancelledAt = null;
        this.failureReason = null;
    }

    private static String stringValue(Map<Object, Object> entries, String fieldName) {
        Object value = entries.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing redis field: " + fieldName);
        }
        return value.toString();
    }

    private static int intValue(Map<Object, Object> entries, String fieldName) {
        return Integer.parseInt(stringValue(entries, fieldName));
    }

    private static LocalDateTime optionalLocalDateTime(Map<Object, Object> entries, String fieldName) {
        String value = optionalString(entries, fieldName);
        return value == null ? null : LocalDateTime.parse(value);
    }

    private static String optionalString(Map<Object, Object> entries, String fieldName) {
        Object value = entries.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return value.toString();
    }
}
