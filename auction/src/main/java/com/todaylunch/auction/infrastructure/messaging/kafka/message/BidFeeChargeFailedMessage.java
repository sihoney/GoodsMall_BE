package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record BidFeeChargeFailedMessage(
        UUID bidId,
        UUID auctionId,
        UUID bidderId,
        String failureReason,
        String failureMessage,
        LocalDateTime failedAt
) {}
