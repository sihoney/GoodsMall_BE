package com.todaylunch.auction.common.exception.domain;

import com.todaylunch.auction.common.exception.CustomException;
import com.todaylunch.auction.common.exception.ErrorCode;

public class AuctionNotOngoingException extends CustomException {

    public AuctionNotOngoingException() {
        super(ErrorCode.AUCTION_NOT_ONGOING);
    }
}
