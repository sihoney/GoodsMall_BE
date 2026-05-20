package com.example.settlement.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String SETTLEMENT_CANDIDATE_CREATED = "payment.settlement-candidate-created";
    public static final String SETTLEMENT_PAYOUT_REQUESTED = "settlement.seller-payout-requested";
    public static final String SETTLEMENT_PAYOUT_RESULT = "payment.seller-payout-result";
    public static final String SETTLEMENT_CANDIDATE_CREATED_DLQ = "payment.settlement-candidate-created.dlq";
    public static final String SETTLEMENT_PAYOUT_RESULT_DLQ = "payment.seller-payout-result.dlq";

    private KafkaTopics() {
    }
}
