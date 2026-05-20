package com.example.payment.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 주문 결제 API 응답 DTO다.
 * 기존 Kafka OrderPaymentResult 계약과 유사한 형태로 반환한다.
 */
public record OrderPaymentApiResponse(
        UUID orderId,
        UUID buyerMemberId,
        BigDecimal amount,
        String status,
        String reasonCode
) {
}
