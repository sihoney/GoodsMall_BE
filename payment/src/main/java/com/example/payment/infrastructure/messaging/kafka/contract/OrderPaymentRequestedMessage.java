package com.example.payment.infrastructure.messaging.kafka.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * order가 payment로 보내는 주문 결제 요청 계약 메시지다.
 * 시간 필드는 UTC 기준 Instant를 사용하고, seller 정보는 orderLines 안에 포함된다.
 */
public record OrderPaymentRequestedMessage(
        String eventId,
        UUID orderId,
        UUID buyerId,
        BigDecimal totalPrice,
        Instant orderCreatedAt,
        Instant eventCreatedAt,
        List<OrderPaymentRequestedLineMessage> orderLines
) {
}
