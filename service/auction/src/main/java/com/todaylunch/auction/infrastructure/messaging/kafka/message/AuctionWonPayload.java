package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionWonPayload(
        String auctionTitle,
        String thumbnailKey,
        BigDecimal finalPrice,
        UUID productId,
        UUID sellerId,
        BigDecimal orderPrice
) {}
