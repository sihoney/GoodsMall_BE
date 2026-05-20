package com.example.payment.common.common.exception;

public class AuctionBidFeeEventValidationException extends CustomException {

    public AuctionBidFeeEventValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuctionBidFeeEventValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
