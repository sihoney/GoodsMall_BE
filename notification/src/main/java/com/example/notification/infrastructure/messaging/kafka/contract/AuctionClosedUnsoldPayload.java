package com.example.notification.infrastructure.messaging.kafka.contract;

public record AuctionClosedUnsoldPayload(
        String auctionTitle
) {
}
