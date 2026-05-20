package com.todaylunch.auction.presentation.dto.response;

import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BidResponse(
        UUID bidId,
        UUID auctionId,
        UUID bidderId,
        BigDecimal bidPrice,
        BidStatus status,
        LocalDateTime createdAt
) {

    public static BidResponse from(Bid bid) {
        return new BidResponse(
                bid.getBidId(),
                bid.getAuction().getAuctionId(),
                bid.getBidderId(),
                bid.getBidPrice(),
                bid.getStatus(),
                bid.getCreatedAt()
        );
    }
}
