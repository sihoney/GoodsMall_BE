package com.example.order.domain.enumtype;

public enum OrderItemStatus {
    PENDING,    // 주문 접수
    PREPARING,  // 상품 준비 중
    SHIPPING,   // 배송 중
    DELIVERED,  // 배송 완료
    COMPLETED,  // 구매 확정
    CANCELED,  // 주문 취소
}