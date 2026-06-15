package com.example.ai.common.exception;

public class AuctionPriceRecommendationProcessingException extends AiAuctionPriceRecommendationException {

    public AuctionPriceRecommendationProcessingException(String message, Throwable cause) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_PROCESSING_ERROR, message, cause);
    }
}

