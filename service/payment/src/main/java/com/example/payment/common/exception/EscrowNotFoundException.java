package com.example.payment.common.exception;

/**
 * ?붿껌??escrow瑜?李얠? 紐삵뻽?????ъ슜?섎뒗 怨듯넻 ?덉쇅??
 */
public class EscrowNotFoundException extends CustomException {

    public EscrowNotFoundException() {
        super(ErrorCode.ESCROW_NOT_FOUND);
    }
}
