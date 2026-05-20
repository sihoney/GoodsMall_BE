package com.example.payment.common.exception;

/**
 * ?붿껌??charge瑜?李얠? 紐삵뻽?????ъ슜?섎뒗 怨듯넻 ?덉쇅??
 */
public class ChargeNotFoundException extends CustomException {

    public ChargeNotFoundException() {
        super(ErrorCode.CHARGE_NOT_FOUND);
    }
}
