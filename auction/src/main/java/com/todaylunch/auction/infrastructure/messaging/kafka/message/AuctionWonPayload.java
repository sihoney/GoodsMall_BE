package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionWonPayload(
        String auctionTitle,
        BigDecimal finalPrice,
        UUID productId,
        BigDecimal orderPrice
) {}
