package com.example.payment.common.exception;

public class InvalidAuctionBidFeeRequestException extends CustomException {

    public InvalidAuctionBidFeeRequestException(String message) {
        super(ErrorCode.INVALID_AUCTION_BID_FEE_REQUEST, message);
    }
}
