package com.example.ai.common.exception;

public class AuctionPriceRecommendationExternalCallException extends AiAuctionPriceRecommendationException {

    public AuctionPriceRecommendationExternalCallException(String message, Throwable cause) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_EXTERNAL_CALL_ERROR, message, cause);
    }
}

