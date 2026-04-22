package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.time.Instant;
import java.util.UUID;

public record AuctionClosedUnsoldMessage(
        UUID eventId,
        String eventType,
        String source,
        UUID aggregateId,
        UUID recipientId,
        Instant occurredAt,
        String traceId,
        String auctionTitle
) {}