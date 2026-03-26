package com.example.payment.common.exception;

/**
 * 요청한 wallet을 찾지 못했을 때 사용하는 공통 예외다.
 */
public class WalletNotFoundException extends CustomException {

    public WalletNotFoundException() {
        super(ErrorCode.WALLET_NOT_FOUND);
    }
}
