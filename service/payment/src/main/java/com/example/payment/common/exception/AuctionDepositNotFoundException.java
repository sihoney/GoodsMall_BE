package com.example.payment.common.exception;

public class AuctionDepositNotFoundException extends CustomException {

    public AuctionDepositNotFoundException() {
        super(ErrorCode.AUCTION_DEPOSIT_NOT_FOUND);
    }

    public AuctionDepositNotFoundException(String message) {
        super(ErrorCode.AUCTION_DEPOSIT_NOT_FOUND, message);
    }
}
