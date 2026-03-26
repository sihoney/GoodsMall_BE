package com.example.payment.infrastructure.messaging.kafka.contract;

import com.example.payment.domain.enumtype.ConfirmationType;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 주문 구매확정 이벤트의 Kafka 계약 메시지다.
 */
public record OrderPurchaseConfirmedMessage(
        String eventId,
        UUID orderId,
        UUID sellerMemberId,
        LocalDateTime confirmedAt,
        ConfirmationType confirmationType
) {
}
