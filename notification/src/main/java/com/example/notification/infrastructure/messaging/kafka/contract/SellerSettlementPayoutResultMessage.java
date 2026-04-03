package com.example.notification.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

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
