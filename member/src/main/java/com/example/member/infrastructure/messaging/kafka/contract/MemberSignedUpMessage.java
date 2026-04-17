package com.example.member.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * member 서비스의 회원 가입 Kafka 계약 메시지다.
 * 발행 토픽: member-signed-up
 *
 * 현재는 내부 이벤트와 동일한 필드 구성을 유지해서
 * 다른 서비스의 기존 consumer를 깨지 않도록 한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberSignedUpMessage(
        UUID eventId,
        UUID memberId,
        String email,
        Instant occurredAt
) {
}
