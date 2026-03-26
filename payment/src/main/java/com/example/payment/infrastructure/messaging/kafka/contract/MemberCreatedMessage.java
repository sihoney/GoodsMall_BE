package com.example.payment.infrastructure.messaging.kafka.contract;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * member 서비스의 회원 생성 이벤트 Kafka 계약 메시지다.
 */
public record MemberCreatedMessage(
        String eventId,
        UUID memberId,
        LocalDateTime occurredAt
) {
}
