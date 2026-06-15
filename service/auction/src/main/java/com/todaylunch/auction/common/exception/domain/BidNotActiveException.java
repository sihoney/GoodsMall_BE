package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class BidNotActiveException extends CustomException {

    public BidNotActiveException() {
        super(ErrorCode.BID_NOT_ACTIVE);
    }
}
