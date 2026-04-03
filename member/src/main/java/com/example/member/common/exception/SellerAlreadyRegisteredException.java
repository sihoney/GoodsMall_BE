package com.example.member.common.exception;

public class SellerAlreadyRegisteredException extends RuntimeException {

    public SellerAlreadyRegisteredException() {
        super("이미 판매자로 등록된 회원입니다.");
    }
}
