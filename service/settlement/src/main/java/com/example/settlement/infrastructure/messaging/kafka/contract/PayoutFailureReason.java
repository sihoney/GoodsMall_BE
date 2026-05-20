package com.example.settlement.infrastructure.messaging.kafka.contract;

/**
 * payment -> settlement 지급 결과 실패 사유 표준 코드(contract)다.
 * <p>
 * 재시도 분류:
 * - NON_RETRYABLE: 자동 재시도 금지, 운영 확인 후 수동 조치
 * - RETRYABLE: 백오프(backoff) 재시도 후보, 동일 settlementId로 멱등 처리 유지
 */
public enum PayoutFailureReason {

    /**
     * 판매자 wallet이 없어 지급 실행 불가 — NON_RETRYABLE
     */
    WALLET_NOT_FOUND(false),

    /**
     * 지급 금액이 0 이하 또는 정책 위반 — NON_RETRYABLE
     */
    INVALID_PAYOUT_AMOUNT(false),

    /**
     * 이미 같은 settlementId가 지급 처리됨 — NON_RETRYABLE
     */
    DUPLICATE_PAYOUT(false),

    /**
     * settlement 식별 불가(결과 반영 시점 불일치) — RETRYABLE
     */
    SETTLEMENT_NOT_FOUND(true),

    /**
     * 일시적 DB 오류/락 경합으로 처리 실패 — RETRYABLE
     */
    TEMPORARY_DB_ERROR(true),

    /**
     * 결과 이벤트 발행 실패 — RETRYABLE
     */
    KAFKA_PUBLISH_ERROR(true),

    /**
     * 위 코드로 분류되지 않은 예외 — RETRYABLE
     */
    INTERNAL_ERROR(true);

    private final boolean retryable;

    PayoutFailureReason(boolean retryable) {
        this.retryable = retryable;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

