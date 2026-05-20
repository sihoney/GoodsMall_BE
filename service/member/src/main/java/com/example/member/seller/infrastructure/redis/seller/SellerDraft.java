package com.example.member.seller.infrastructure.redis.seller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class SellerDraft {

    private static final String FIELD_DRAFT_ID = "draftId";
    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_SESSION_ID = "sessionId";
    private static final String FIELD_BANK_NAME = "bankName";
    private static final String FIELD_ENCRYPTED_ACCOUNT_NUMBER = "encryptedAccountNumber";
    private static final String FIELD_ACCOUNT_NUMBER_MASKED = "accountNumberMasked";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_UPDATED_AT = "updatedAt";

    private final String draftId;
    private final UUID memberId;
    private final String sessionId;
    private final String bankName;
    private final String encryptedAccountNumber;
    private final String accountNumberMasked;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private SellerDraft(
            String draftId,
            UUID memberId,
            String sessionId,
            String bankName,
            String encryptedAccountNumber,
            String accountNumberMasked,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.draftId = Objects.requireNonNull(draftId);
        this.memberId = Objects.requireNonNull(memberId);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.bankName = Objects.requireNonNull(bankName);
        this.encryptedAccountNumber = Objects.requireNonNull(encryptedAccountNumber);
        this.accountNumberMasked = Objects.requireNonNull(accountNumberMasked);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static SellerDraft create(
            String draftId,
            UUID memberId,
            String sessionId,
            String bankName,
            String encryptedAccountNumber,
            String accountNumberMasked,
            LocalDateTime createdAt
    ) {
        return new SellerDraft(
                draftId,
                memberId,
                sessionId,
                bankName,
                encryptedAccountNumber,
                accountNumberMasked,
                "PENDING",
                createdAt,
                createdAt
        );
    }

    public static SellerDraft fromMap(Map<Object, Object> entries) {
        return new SellerDraft(
                stringValue(entries, FIELD_DRAFT_ID),
                UUID.fromString(stringValue(entries, FIELD_MEMBER_ID)),
                stringValue(entries, FIELD_SESSION_ID),
                stringValue(entries, FIELD_BANK_NAME),
                stringValue(entries, FIELD_ENCRYPTED_ACCOUNT_NUMBER),
                stringValue(entries, FIELD_ACCOUNT_NUMBER_MASKED),
                stringValue(entries, FIELD_STATUS),
                LocalDateTime.parse(stringValue(entries, FIELD_CREATED_AT)),
                LocalDateTime.parse(stringValue(entries, FIELD_UPDATED_AT))
        );
    }

    public Map<String, String> toMap() {
        Map<String, String> values = new HashMap<>();
        values.put(FIELD_DRAFT_ID, draftId);
        values.put(FIELD_MEMBER_ID, memberId.toString());
        values.put(FIELD_SESSION_ID, sessionId);
        values.put(FIELD_BANK_NAME, bankName);
        values.put(FIELD_ENCRYPTED_ACCOUNT_NUMBER, encryptedAccountNumber);
        values.put(FIELD_ACCOUNT_NUMBER_MASKED, accountNumberMasked);
        values.put(FIELD_STATUS, status);
        values.put(FIELD_CREATED_AT, createdAt.toString());
        values.put(FIELD_UPDATED_AT, updatedAt.toString());
        return values;
    }

    private static String stringValue(Map<Object, Object> entries, String fieldName) {
        Object value = entries.get(fieldName);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Redis 필드가 누락되었습니다: " + fieldName);
        }
        return value.toString();
    }
}
