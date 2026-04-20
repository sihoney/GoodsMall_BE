package com.example.ai.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionPriceRecommendationCommand(
        UUID auctionId,
        UUID productId,
        BigDecimal currentBidPrice,
        BigDecimal startPrice,
        String productName,
        Integer bidCount,
        Long remainingSeconds
) {
}

