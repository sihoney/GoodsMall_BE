package com.todaylunch.auction.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String BID_FEE_CHARGE_REQUESTED = "auction.bid-fee.charge.requested";
    public static final String BID_FEE_CHARGE_COMPLETED = "payment.bid-fee.charge.succeeded";
    public static final String BID_FEE_CHARGE_FAILED    = "payment.bid-fee.charge.failed";

    private KafkaTopics() {}
}
