package com.example.order.domain.enumtype;

public enum OrderItemStatus {
    PENDING,            // 주문 접수
    PREPARING,          // 상품 준비 중
    SHIPPING,           // 배송 중
    DELIVERED,          // 배송 완료
    COMPLETED,          // 구매 확정
    CANCELED,           // 주문 취소
    RETURN_REQUESTED,   // 반품 신청 (검수 대기/진행)
}