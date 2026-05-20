package com.example.payment.common.infrastructure.messaging.kafka.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

/**
 * member ?쒕퉬?ㅼ쓽 ?뚯썝 媛???대깽??MemberSignedUpEvent) Kafka 怨꾩빟 硫붿떆吏??
 * 諛쒗뻾 ?좏뵿: member-signed-up
 * 諛쒗뻾 痢??대깽?? MemberSignedUpEvent (member 紐⑤뱢)
 *
 * 怨꾩빟 ?꾨뱶??諛쒗뻾 痢?MemberSignedUpEvent ? ?꾩쟾???쇱튂?쒕떎.
 * ?ν썑 諛쒗뻾 痢≪뿉 ?꾨뱶媛 異붽??섎뜑?쇰룄 ??쭅?ы솕媛 源⑥?吏 ?딅룄濡? * {@code @JsonIgnoreProperties(ignoreUnknown = true)} 瑜??곸슜?쒕떎.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record MemberCreatedMessage(
        UUID eventId,
        UUID memberId,
        String email,
        Instant occurredAt
) {
}
