package com.todaylunch.auction.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidPlacedEvent(
        UUID auctionId,
        UUID bidderId,
        BigDecimal bidPrice,
        LocalDateTime endAt
) {
}
