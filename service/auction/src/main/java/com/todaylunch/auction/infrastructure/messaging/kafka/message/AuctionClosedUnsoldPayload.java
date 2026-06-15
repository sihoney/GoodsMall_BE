package com.todaylunch.auction.infrastructure.messaging.kafka.message;

public record AuctionClosedUnsoldPayload(
        String auctionTitle
) {}
