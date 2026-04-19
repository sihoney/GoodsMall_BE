package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class SelfBidNotAllowedException extends CustomException {

    public SelfBidNotAllowedException() {
        super(ErrorCode.SELF_BID_NOT_ALLOWED);
    }
}
