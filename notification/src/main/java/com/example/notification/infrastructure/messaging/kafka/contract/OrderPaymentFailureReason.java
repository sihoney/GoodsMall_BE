package com.example.notification.infrastructure.messaging.kafka.contract;

public enum OrderPaymentFailureReason {
    DUPLICATE_ORDER_PAYMENT,    // 중복된 주문 결제 시도
    WALLET_NOT_FOUND,           // 지갑 정보가 존재하지 않는 경우
    INSUFFICIENT_BALANCE,       // 지갑 잔액 부족
    INVALID_REQUEST,            // 잘못된 요청 (예: 주문 정보 누락, 형식 오류 등)
    INTERNAL_ERROR              // 내부 시스템 오류 (예: 결제 처리 중 예외 발생 등)
}
