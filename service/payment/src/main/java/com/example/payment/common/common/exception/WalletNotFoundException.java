package com.example.payment.common.common.exception;

/**
 * ?붿껌??wallet??李얠? 紐삵뻽?????ъ슜?섎뒗 怨듯넻 ?덉쇅??
 */
public class WalletNotFoundException extends CustomException {

    public WalletNotFoundException() {
        super(ErrorCode.WALLET_NOT_FOUND);
    }
}
