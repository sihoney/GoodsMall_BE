package com.todaylunch.auction.infrastructure.messaging.kafka.message;

import java.math.BigDecimal;

public record AuctionWonPayload(
        String auctionTitle,
        BigDecimal finalPrice
) {}
