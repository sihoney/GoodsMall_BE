package com.example.payment.common.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * auction -> payment 寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 Kafka 怨꾩빟 硫붿떆吏??
 */
public record BidFeeChargeRequestMessage(
        UUID bidId,
        UUID auctionId,
        UUID highestBidderId,
        BigDecimal highestBidderFee
) {
}
