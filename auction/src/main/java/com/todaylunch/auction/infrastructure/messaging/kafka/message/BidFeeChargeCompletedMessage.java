package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.time.Instant;
import java.util.UUID;

public record BidFeeChargeCompletedMessage(
        UUID eventId,
        UUID bidId,
        UUID auctionId,
        Instant occurredAt
) {}
