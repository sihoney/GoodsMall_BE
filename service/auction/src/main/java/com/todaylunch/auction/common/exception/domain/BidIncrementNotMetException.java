package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidIncrementNotMetException extends CustomException {

    public BidIncrementNotMetException() {
        super(ErrorCode.BID_INCREMENT_NOT_MET);
    }
}
