package com.example.payment.common.exception;

public class WalletNotFoundException extends CustomException {

    public WalletNotFoundException() {
        super(ErrorCode.WALLET_NOT_FOUND);
    }
}
