package com.example.ai.common.exception;

public class AiAuctionPriceRecommendationException extends CustomException {

    public AiAuctionPriceRecommendationException() {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_ERROR);
    }

    public AiAuctionPriceRecommendationException(String message) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_ERROR, message);
    }

    protected AiAuctionPriceRecommendationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    protected AiAuctionPriceRecommendationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}

