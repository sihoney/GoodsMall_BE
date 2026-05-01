package com.example.order.domain.enumtype;

public enum RequesterType {
    BUYER,  // 구매자
    SELLER, // 판매자
    ADMIN,  // 관리자
    ;

    public ResponsibilityType toResponsibility() {
        return switch (this) {
            case BUYER -> ResponsibilityType.BUYER;
            case SELLER -> ResponsibilityType.SELLER;
            case ADMIN -> ResponsibilityType.ADMIN;
        };
    }
}
