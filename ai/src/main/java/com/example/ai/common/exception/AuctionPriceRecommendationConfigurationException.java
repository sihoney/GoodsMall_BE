package com.example.ai.common.exception;

public class AuctionPriceRecommendationConfigurationException extends AiAuctionPriceRecommendationException {

    public AuctionPriceRecommendationConfigurationException(String message) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_CONFIGURATION_ERROR, message);
    }
}

