package com.example.notification.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;

public record AuctionClosedSoldPayload(
        String auctionTitle,
        BigDecimal finalPrice
) {
}
