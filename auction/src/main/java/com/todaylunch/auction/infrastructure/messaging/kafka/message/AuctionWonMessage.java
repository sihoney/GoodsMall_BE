package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AuctionWonMessage(
        UUID eventId,
        String eventType,
        String source,
        UUID aggregateId,
        UUID recipientId,
        Instant occurredAt,
        String traceId,
        String auctionTitle,
        BigDecimal finalPrice
) {}