package com.example.payment.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * member 서비스의 회원 가입 이벤트(MemberSignedUpEvent) Kafka 계약 메시지다.
 * 발행 토픽: member-signed-up
 * 발행 측 이벤트: MemberSignedUpEvent (member 모듈)
 *
 * 계약 필드는 발행 측 MemberSignedUpEvent 와 완전히 일치한다.
 * 향후 발행 측에 필드가 추가되더라도 역직렬화가 깨지지 않도록
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} 를 적용한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberCreatedMessage(
        UUID eventId,
        UUID memberId,
        String email,
        Instant occurredAt
) {
}
