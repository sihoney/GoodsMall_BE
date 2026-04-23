package com.example.notification.domain.enumtype;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum NotificationType {
    BUYER_SIGNUP_COMPLETED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_PROMOTED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    ACCOUNT_VERIFICATION_EXPIRED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    ACCOUNT_VERIFICATION_FAILED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    MEMBER_OAUTH_LINKED(NotificationChannel.INBOX),
    BUYER_ORDER_CREATED(NotificationChannel.INBOX),
    BUYER_ORDER_CANCELED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    BUYER_AUTO_PURCHASE_CONFIRMED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    BUYER_ORDER_PAYMENT_SUCCEEDED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    BUYER_ORDER_PAYMENT_FAILED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_ORDER_RECEIVED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_ORDER_CANCELED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_SETTLEMENT_PAYOUT_SUCCEEDED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_SETTLEMENT_PAYOUT_FAILED(NotificationChannel.INBOX, NotificationChannel.PUSH),
    BUYER_AUCTION_OUTBID(NotificationChannel.INBOX, NotificationChannel.PUSH),
    BUYER_AUCTION_WON(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_AUCTION_CLOSED_SOLD(NotificationChannel.INBOX, NotificationChannel.PUSH),
    SELLER_AUCTION_CLOSED_UNSOLD(NotificationChannel.INBOX, NotificationChannel.PUSH);

    private final Set<NotificationChannel> channels;

    NotificationType(NotificationChannel... channels) {
        if (channels == null || channels.length == 0) {
            throw new IllegalArgumentException("NotificationType must define at least one channel.");
        }
        this.channels = Collections.unmodifiableSet(EnumSet.of(channels[0], channels));
    }

    public boolean supportsChannel(NotificationChannel channel) {
        return channels.contains(channel);
    }

    public Set<NotificationChannel> channels() {
        return channels;
    }
}
