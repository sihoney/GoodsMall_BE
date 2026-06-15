package com.example.notification.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String MEMBER_SIGNED_UP = "member.signed-up";
    public static final String SELLER_PROMOTED = "member.seller-promoted";
    public static final String ACCOUNT_VERIFICATION_EXPIRED = "member.account-verification-expired";
    public static final String ACCOUNT_VERIFICATION_FAILED = "member.account-verification-failed";
    public static final String MEMBER_OAUTH_LINKED = "member.oauth-linked";
    public static final String ORDER_CREATED = "order.created";
    public static final String ORDER_CANCELED = "order.canceled";
    public static final String AUTO_PURCHASE_CONFIRMED = "payment.auto-purchase-confirmed";
    public static final String ORDER_PAYMENT_RESULT = "payment.order-payment-result";
    public static final String SELLER_SETTLEMENT_PAYOUT_RESULT = "payment.seller-payout-result";
    public static final String AUCTION_BID_OUTBID = "auction.bid.outbid";
    public static final String AUCTION_WON = "auction.won";
    public static final String AUCTION_CLOSED = "auction.closed";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    private KafkaTopics() {
    }
}
