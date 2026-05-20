package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class HighestBidderRebidNotAllowedException extends CustomException {

    public HighestBidderRebidNotAllowedException() {
        super(ErrorCode.HIGHEST_BIDDER_CANNOT_REBID);
    }
}
