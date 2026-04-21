package com.example.payment.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String AUCTION_BID_FEE_CHARGE_REQUESTED = "auction.bid-fee.charge.requested";
    public static final String AUCTION_BID_FEE_CHARGE_SUCCEEDED = "payment.bid-fee.charge.succeeded";
    public static final String AUCTION_BID_FEE_CHARGE_FAILED = "payment.bid-fee.charge.failed";

    private KafkaTopics() {
    }
}
