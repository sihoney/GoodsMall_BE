package com.example.settlement.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * settlement -> payment 정산 지급 요청 이벤트 계약(contract)이다.
 */
public record SellerSettlementPayoutRequestedMessage(
        UUID eventId,
        UUID settlementId,
        UUID sellerMemberId,
        Integer settlementYear,
        Integer settlementMonth,
        Long payoutAmount,
        LocalDateTime requestedAt
) {
}

