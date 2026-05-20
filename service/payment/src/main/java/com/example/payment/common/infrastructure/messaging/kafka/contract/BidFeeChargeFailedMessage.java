package com.example.payment.common.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

/**
 * payment -> auction 寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?ㅽ뙣 Kafka 怨꾩빟 硫붿떆吏??
 */
public record BidFeeChargeFailedMessage(
        UUID eventId,
        UUID bidId,
        UUID auctionId,
        String errorCode,
        String errorMessage,
        Instant occurredAt
) {
}
