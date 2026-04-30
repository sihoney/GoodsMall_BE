package com.todaylunch.auction.presentation.dto.response;

public record AuctionSellerBlockingSummaryResponse(
        boolean waiting,
        boolean ongoing,
        boolean pendingPayment
) {
}
