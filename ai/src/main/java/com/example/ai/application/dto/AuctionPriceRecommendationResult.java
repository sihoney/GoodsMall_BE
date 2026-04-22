package com.example.ai.application.dto;

import java.math.BigDecimal;

public record AuctionPriceRecommendationResult(
        BigDecimal expectedFinalPrice,
        BigDecimal recommendedBidPrice,
        String priceReason,
        String notes
) {
}

