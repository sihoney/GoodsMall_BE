package com.example.ai.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionPriceRecommendationCommand(
        UUID auctionId,
        UUID productId,
        String productName,
        BigDecimal currentBidPrice,
        BigDecimal startPrice,
        BigDecimal bidUnit,
        BigDecimal nextMinimumBidPrice,
        Integer bidCount,
        Long remainingSeconds,
        String auctionStatus,
        Boolean hasBid
) {
}

