package com.todaylunch.auction.application.port;

import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.application.port.dto.response.BidFeeChargeResponse;

public interface BidFeeChargePort {

    BidFeeChargeResponse chargeBidFee(BidFeeChargeRequest request);
}
