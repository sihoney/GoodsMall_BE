package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

/**
 * payment -> auction 경매 입찰 보증금 처리 실패 Kafka 계약 메시지다.
 */
public record BidFeeChargeFailedMessage(
        UUID eventId,
        UUID bidId,
        UUID auctionId,
        String errorCode,
        String errorMessage,
        Instant occurredAt
) {
}
