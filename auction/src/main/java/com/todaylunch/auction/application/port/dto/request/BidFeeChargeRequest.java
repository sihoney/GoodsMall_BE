package com.todaylunch.auction.application.port.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record BidFeeChargeRequest(
        UUID auctionId,
        UUID previousBidderId,
        BigDecimal previousBidderPaidFee,
        UUID highestBidderId,
        BigDecimal highestBidderFee
) {}
