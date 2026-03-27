package com.example.member.common.exception;

public class SellerNotFoundException extends RuntimeException {

    public SellerNotFoundException() {
        super("판매자 정보를 찾을 수 없습니다.");
    }
}
