package com.example.order.infrastructure.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionWonEvent(
        UUID productId,
        UUID sellerId,
        String productTitle,
        String thumbnailKey,
        BigDecimal orderPrice
) {}
