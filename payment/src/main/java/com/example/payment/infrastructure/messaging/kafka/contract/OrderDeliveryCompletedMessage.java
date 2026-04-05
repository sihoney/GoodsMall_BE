package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.Instant;
import java.util.UUID;

/**
 * order 배송완료 이벤트의 Kafka 계약 메시지다.
 */
// todo : 이벤트 발행하는 order랑 맞춰서 필드 값을 지정한다.
// 에크스로 해제 시점을 기록하기 위해서 주문 번호와 배송 완료 시간은 필요
public record OrderDeliveryCompletedMessage(
        String eventId,
        UUID orderId,
        Instant deliveredAt,
        Instant occurredAt
) {
}
