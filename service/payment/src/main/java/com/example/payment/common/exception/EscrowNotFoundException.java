package com.example.payment.common.exception;

/**
 * 요청한 escrow를 찾지 못했을 때 사용하는 공통 예외다.
 */
public class EscrowNotFoundException extends CustomException {

    public EscrowNotFoundException() {
        super(ErrorCode.ESCROW_NOT_FOUND);
    }
}
