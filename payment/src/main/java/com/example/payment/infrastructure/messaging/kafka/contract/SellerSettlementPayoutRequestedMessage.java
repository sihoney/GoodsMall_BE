package com.example.payment.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * settlement -> payment 정산 지급 요청 이벤트 계약(contract)이다.
 */
public record SellerSettlementPayoutRequestedMessage(
        UUID eventId,
        UUID settlementId,
        SettlementPayoutType settlementType,
        UUID sellerMemberId,
        Integer settlementYear,
        Integer settlementMonth,
        BigDecimal payoutAmount,
        LocalDateTime requestedAt
) {
}

