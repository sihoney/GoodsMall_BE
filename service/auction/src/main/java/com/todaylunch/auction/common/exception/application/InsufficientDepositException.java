package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class InsufficientDepositException extends CustomException {

    public InsufficientDepositException() {
        super(ErrorCode.INSUFFICIENT_DEPOSIT);
    }
}
