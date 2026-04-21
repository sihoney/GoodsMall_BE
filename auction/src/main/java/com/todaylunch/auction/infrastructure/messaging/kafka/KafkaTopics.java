package com.todaylunch.auction.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String BID_FEE_CHARGE_REQUESTED = "auction.bid-fee.charge-requested";
    public static final String BID_FEE_CHARGE_COMPLETED = "auction.bid-fee.charge-completed";
    public static final String BID_FEE_CHARGE_FAILED    = "auction.bid-fee.charge-failed";

    private KafkaTopics() {}
}
