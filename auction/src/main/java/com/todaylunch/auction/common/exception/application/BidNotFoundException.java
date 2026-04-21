package com.todaylunch.auction.common.exception.application;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidNotFoundException extends CustomException {

    public BidNotFoundException() {
        super(ErrorCode.BID_NOT_FOUND);
    }
}
