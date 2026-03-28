package com.example.settlement.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * settlement API 오류 응답의 표준 코드와 기본 메시지를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "정산 정보를 찾을 수 없습니다."),
    MANUAL_RETRY_NOT_ALLOWED(HttpStatus.CONFLICT, "현재 상태/사유에서는 수동 재지급을 허용하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
