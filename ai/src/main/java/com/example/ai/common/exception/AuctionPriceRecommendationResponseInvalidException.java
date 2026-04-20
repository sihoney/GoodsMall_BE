package com.example.ai.common.exception;

public class AuctionPriceRecommendationResponseInvalidException extends AiAuctionPriceRecommendationException {

    public AuctionPriceRecommendationResponseInvalidException(String message) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_RESPONSE_INVALID_ERROR, message);
    }

    public AuctionPriceRecommendationResponseInvalidException(String message, Throwable cause) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_RESPONSE_INVALID_ERROR, message, cause);
    }
}

