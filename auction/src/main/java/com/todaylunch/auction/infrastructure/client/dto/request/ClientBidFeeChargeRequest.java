package com.todaylunch.auction.infrastructure.client.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ClientBidFeeChargeRequest(
        UUID auctionId,
        boolean isFirst,
        UUID previousBidderId,
        BigDecimal previousBidderPaidFee,
        UUID highestBidderId,
        BigDecimal highestBidderFee
) {}
