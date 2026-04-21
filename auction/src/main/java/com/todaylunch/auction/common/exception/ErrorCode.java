package com.todaylunch.auction.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),

    // Application
    AUCTION_NOT_FOUND(HttpStatus.NOT_FOUND, "경매를 찾을 수 없습니다"),

    // Domain
    AUCTION_NOT_ONGOING(HttpStatus.CONFLICT, "진행 중인 경매가 아닙니다"),
    SELF_BID_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자신의 경매에는 입찰할 수 없습니다"),
    BID_INCREMENT_NOT_MET(HttpStatus.BAD_REQUEST, "입찰가는 이전가와 최소 입찰 단위의 합 이상이어야 합니다"),
    BID_PRICE_UNIT_NOT_MET(HttpStatus.BAD_REQUEST, "입찰가는 100원 단위여야 합니다"),
    INVALID_BID_PRICE(HttpStatus.BAD_REQUEST, "입찰가는 0보다 커야 합니다"),
    BID_NOT_ACTIVE(HttpStatus.CONFLICT, "활성 상태의 입찰이 아닙니다");

    private final HttpStatus httpStatus;
    private final String message;
}
