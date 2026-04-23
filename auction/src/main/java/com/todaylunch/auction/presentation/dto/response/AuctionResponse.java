package com.todaylunch.auction.presentation.dto.response;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AuctionResponse(
        UUID auctionId,
        UUID productId,
        String productTitle,
        UUID sellerId,
        BigDecimal startPrice,
        BigDecimal bidUnit,
        BigDecimal currentHighestPrice,
        LocalDateTime startedAt,
        LocalDateTime scheduledCloseAt,
        LocalDateTime endedAt,
        AuctionStatus status,
        LocalDateTime createdAt
) {

    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(
                auction.getAuctionId(),
                auction.getProductId(),
                auction.getProductTitle(),
                auction.getSellerId(),
                auction.getStartPrice(),
                auction.getBidUnit(),
                auction.getCurrentHighestPrice(),
                auction.getStartedAt(),
                auction.getScheduledCloseAt(),
                auction.getEndedAt(),
                auction.getStatus(),
                auction.getCreatedAt()
        );
    }
}
