package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

/**
 * payment -> auction 경매 입찰 보증금 처리 성공 Kafka 계약 메시지다.
 */
public record BidFeeChargeSucceededMessage(
        UUID eventId,
        UUID auctionId,
        Instant occurredAt
) {
}
