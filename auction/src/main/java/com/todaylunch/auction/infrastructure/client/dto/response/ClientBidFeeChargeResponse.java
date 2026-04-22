package com.todaylunch.auction.infrastructure.client.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ClientBidFeeChargeResponse(
        UUID auctionId,
        UUID highestBidderId,
        BigDecimal heldAmount,
        UUID previousBidderId,
        BigDecimal refundedAmount
) {}
