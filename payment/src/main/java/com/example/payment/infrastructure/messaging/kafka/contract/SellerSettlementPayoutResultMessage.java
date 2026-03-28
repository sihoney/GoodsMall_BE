package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * payment -> settlement 정산 지급 결과 이벤트 계약(contract)이다.
 */
public record SellerSettlementPayoutResultMessage(
        UUID eventId,
        UUID requestEventId,
        UUID settlementId,
        UUID sellerMemberId,
        Long payoutAmount,
        SellerSettlementPayoutResultStatus resultStatus,
        String failureReason,
        LocalDateTime processedAt
) {
}

