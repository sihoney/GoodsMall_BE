package com.example.member.common.infrastructure.messaging.kafka;

public final class KafkaTopics {

    public static final String MEMBER_SIGNED_UP = "member.signed-up";
    public static final String SELLER_PROMOTED = "member.seller-promoted";
    public static final String ACCOUNT_VERIFICATION_EXPIRED = "member.account-verification-expired";
    public static final String ACCOUNT_VERIFICATION_FAILED = "member.account-verification-failed";
    public static final String MEMBER_OAUTH_LINKED = "member.oauth-linked";

    private KafkaTopics() {
    }
}
