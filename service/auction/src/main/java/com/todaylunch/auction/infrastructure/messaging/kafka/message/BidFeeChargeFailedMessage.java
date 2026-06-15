package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.time.Instant;
import java.util.UUID;

public record BidFeeChargeFailedMessage(
        UUID eventId,
        UUID bidId,
        UUID auctionId,
        String errorCode,
        String errorMessage,
        Instant occurredAt
) {}
