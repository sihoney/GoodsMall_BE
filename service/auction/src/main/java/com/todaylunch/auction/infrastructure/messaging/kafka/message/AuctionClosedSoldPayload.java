package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.math.BigDecimal;

public record AuctionClosedSoldPayload(
        String auctionTitle,
        BigDecimal finalPrice
) {}
