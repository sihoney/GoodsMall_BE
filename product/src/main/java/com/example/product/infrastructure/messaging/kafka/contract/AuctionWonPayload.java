package com.example.product.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionWonPayload(
        String auctionTitle,
        BigDecimal finalPrice,
        UUID productId,
        BigDecimal orderPrice
) {}
