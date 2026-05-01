package com.example.order.domain.enumtype;

public enum DeliveryStatus {
    PREPARING,  // 상품 준비중
    SHIPPED,    // 배달 중
    DELIVERED,  // 배달 완료
    CANCELED,   // 주문 취소로 인한 배송 취소
}
