package com.example.ai.common.exception;

public class AuctionPriceRecommendationRequestInvalidException extends CustomException {

    public AuctionPriceRecommendationRequestInvalidException(String message) {
        super(ErrorCode.AI_AUCTION_PRICE_RECOMMENDATION_REQUEST_INVALID, message);
    }
}

