package com.todaylunch.auction.application.port.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record BidFeeChargeResponse(
        UUID auctionId,
        UUID highestBidderId,
        BigDecimal heldAmount,
        UUID previousBidderId,
        BigDecimal refundedAmount
) {}
