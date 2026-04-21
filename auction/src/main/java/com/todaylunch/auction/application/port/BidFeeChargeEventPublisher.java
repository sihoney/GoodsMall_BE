package com.todaylunch.auction.application.port;

import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;

public interface BidFeeChargeEventPublisher {

    void publish(BidFeeChargeRequest request);
}
