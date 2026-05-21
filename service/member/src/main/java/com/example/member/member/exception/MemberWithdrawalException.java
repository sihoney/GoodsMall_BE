package com.example.member.member.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MemberWithdrawalException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public MemberWithdrawalException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }
}
