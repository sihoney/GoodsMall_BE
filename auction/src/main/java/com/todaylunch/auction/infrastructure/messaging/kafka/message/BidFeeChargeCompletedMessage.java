package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidFeeChargeCompletedMessage(
        UUID bidId,
        UUID auctionId,
        UUID highestBidderId,
        BigDecimal heldAmount,
        UUID previousBidderId,
        BigDecimal refundedAmount,
        LocalDateTime processedAt
) {}
