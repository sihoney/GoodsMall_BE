package com.example.order.domain.enumtype;

public enum OrderStatus {
    PENDING_PAYMENT,   // 주문 생성, 결제 대기
    PAID,              // 결제 완료
    SHIPPING,          // 전체 배송 중
    PARTIAL_SHIPPING,  // 일부 배송 중
    COMPLETED,         // 전체 완료
    PARTIAL_CANCELED,  // 일부 취소
    CANCELED           // 전체 취소
}