package com.example.ai.presentation.dto.response;

import com.example.ai.application.dto.AuctionPriceRecommendationResult;
import java.math.BigDecimal;

public record AuctionPriceRecommendationResponse(
        BigDecimal expectedFinalPrice,
        BigDecimal recommendedBidPrice,
        String priceReason,
        String notes
) {

    public static AuctionPriceRecommendationResponse from(AuctionPriceRecommendationResult result) {
        return new AuctionPriceRecommendationResponse(
                result.expectedFinalPrice(),
                result.recommendedBidPrice(),
                result.priceReason(),
                result.notes()
        );
    }
}

