package com.example.notification.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;

public record AuctionWonPayload(
        String auctionTitle,
        BigDecimal finalPrice
) {
}
