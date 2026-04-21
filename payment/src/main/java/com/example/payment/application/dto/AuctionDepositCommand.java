package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionDepositCommand(
        UUID auctionId,
        UUID previousBidderId,
        BigDecimal previousBidderPaidFee,
        UUID highestBidderId,
        BigDecimal highestBidderFee
) {
}
