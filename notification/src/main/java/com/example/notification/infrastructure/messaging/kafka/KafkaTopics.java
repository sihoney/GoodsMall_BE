package com.example.notification.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String MEMBER_SIGNED_UP = "member-signed-up";
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELED = "order.canceled";
    public static final String AUTO_PURCHASE_CONFIRMED = "payment.auto-purchase-confirmed";
    public static final String ORDER_PAYMENT_RESULT = "payment.order-payment-result";
    public static final String SELLER_SETTLEMENT_PAYOUT_RESULT = "payment.seller-payout-result";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    private KafkaTopics() {
    }
}
