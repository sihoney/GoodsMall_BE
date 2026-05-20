package com.example.order.domain.enumtype;

public enum ReturnRequestStatus {
    REQUESTED,        // 반품 요청됨
    PICKUP_REQUESTED, // 수거 요청됨
    PICKED_UP,        // 수거 완료
    RECEIVED,         // 입고 완료
    COMPLETED,        // 반품 완료
    FAILED,           // 반품 실패
}
