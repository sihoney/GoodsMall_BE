package com.example.ai.infrastructure.client.dto.response;

import java.math.BigDecimal;

public record AuctionPriceRecommendationAiResponse(
        BigDecimal expectedFinalPrice,
        BigDecimal recommendedBidPrice,
        String priceReason
) {
}

