package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionDepositCommand(
        UUID bidId,
        UUID auctionId,
        boolean isFirst,
        UUID previousBidderId,
        BigDecimal previousBidderPaidFee,
        UUID highestBidderId,
        BigDecimal highestBidderFee
) {
}
