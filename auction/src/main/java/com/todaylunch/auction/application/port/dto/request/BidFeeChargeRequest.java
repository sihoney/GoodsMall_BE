package com.todaylunch.auction.application.port.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record BidFeeChargeRequest(
        UUID bidId,
        UUID auctionId,
        boolean isFirst,
        UUID previousBidderId,
        BigDecimal previousBidderPaidFee,
        UUID highestBidderId,
        BigDecimal highestBidderFee
) {
    public BidFeeChargeRequest {
        if (bidId == null) {
            throw new IllegalArgumentException("bidId는 필수입니다");
        }
        if (isFirst && (previousBidderId != null || previousBidderPaidFee != null)) {
            throw new IllegalArgumentException("최초 입찰이면 이전 입찰자 정보가 없어야 합니다");
        }
        if (!isFirst && (previousBidderId == null || previousBidderPaidFee == null)) {
            throw new IllegalArgumentException("재입찰이면 이전 입찰자 정보가 필요합니다");
        }
    }
}
