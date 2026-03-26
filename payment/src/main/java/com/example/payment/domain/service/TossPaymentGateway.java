package com.example.payment.domain.service;

import java.time.LocalDateTime;

/**
 * 외부 결제 게이트웨이 승인/취소를 추상화한 도메인 서비스 포트다.
 */
public interface TossPaymentGateway {

    TossPaymentConfirmation confirm(String paymentKey, String orderId, Long amount);

    TossPaymentCancellation cancel(String paymentKey, String cancelReason, Long cancelAmount);

    /**
     * 승인 API 응답에서 application이 사용하는 최소 필드만 담는 결과 타입이다.
     */
    record TossPaymentConfirmation(
            String paymentKey,
            String orderId,
            Long approvedAmount,
            LocalDateTime approvedAt
    ) {
    }

    /**
     * 취소 API 응답에서 application이 사용하는 최소 필드만 담는 결과 타입이다.
     */
    record TossPaymentCancellation(
            String paymentKey,
            Long canceledAmount,
            LocalDateTime canceledAt
    ) {
    }
}
