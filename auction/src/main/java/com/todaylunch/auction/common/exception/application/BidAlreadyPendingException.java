package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidAlreadyPendingException extends CustomException {

    public BidAlreadyPendingException() {
        super(ErrorCode.BID_ALREADY_PENDING);
    }
}
