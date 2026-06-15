package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class DepositStateConflictException extends CustomException {

    public DepositStateConflictException() {
        super(ErrorCode.DEPOSIT_STATE_CONFLICT);
    }
}
