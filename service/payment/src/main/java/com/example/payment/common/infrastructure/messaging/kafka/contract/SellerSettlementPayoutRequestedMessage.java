package com.example.payment.common.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * settlement -> payment ?뺤궛 吏湲??붿껌 ?대깽??怨꾩빟(contract)?대떎.
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

