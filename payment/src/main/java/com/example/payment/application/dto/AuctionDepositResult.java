package com.example.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuctionDepositResult(
        UUID bidId,
        UUID auctionId,
        UUID highestBidderId,
        BigDecimal heldAmount,
        UUID previousBidderId,
        BigDecimal refundedAmount
) {
}
