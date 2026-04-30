package com.example.member.infrastructure.client.dto.response;

public record AuctionSellerBlockingSummaryResponse(
        boolean waiting,
        boolean ongoing,
        boolean pendingPayment
) {
}
