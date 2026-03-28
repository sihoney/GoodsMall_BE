package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * payment -> settlement 정산 지급 결과 이벤트 계약(contract)이다.
 * <p>
 * 추적 키 기준:
 * - eventId: 이 이벤트의 고유 식별자
 * - requestEventId: 원인 요청 이벤트(SellerSettlementPayoutRequestedMessage)의 eventId — 시도 이력 추적
 * - settlementId: 정산 멱등 기본 키 — 지급 1회성 보장
 */
public record SellerSettlementPayoutResultMessage(
        UUID eventId,
        UUID requestEventId,
        UUID settlementId,
        UUID sellerMemberId,
        Long payoutAmount,
        SellerSettlementPayoutResultStatus resultStatus,
        PayoutFailureReason failureReason,
        LocalDateTime processedAt
) {
}

