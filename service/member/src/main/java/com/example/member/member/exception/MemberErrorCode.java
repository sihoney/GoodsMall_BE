package com.example.member.member.exception;

import com.example.member.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements ErrorCode {
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    DUPLICATE_MEMBER_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_MEMBER_EMAIL", "이미 사용 중인 이메일입니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "INVALID_CURRENT_PASSWORD", "현재 비밀번호가 올바르지 않습니다."),
    MEMBER_WITHDRAWN(HttpStatus.FORBIDDEN, "MEMBER_WITHDRAWN", "탈퇴한 계정입니다. 같은 이메일로 다시 가입해 주세요."),
    MEMBER_SUSPENDED(HttpStatus.FORBIDDEN, "MEMBER_SUSPENDED", "정지된 회원 계정입니다."),
    MEMBER_WITHDRAWAL_PASSWORD_INVALID(
            HttpStatus.BAD_REQUEST,
            "MEMBER_WITHDRAWAL_PASSWORD_INVALID",
            "현재 비밀번호가 올바르지 않습니다."
    ),
    MEMBER_WITHDRAWAL_ADMIN_FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "MEMBER_WITHDRAWAL_ADMIN_FORBIDDEN",
            "관리자 계정은 회원 탈퇴를 지원하지 않습니다."
    ),
    MEMBER_ALREADY_WITHDRAWN(HttpStatus.CONFLICT, "MEMBER_ALREADY_WITHDRAWN", "이미 탈퇴한 회원입니다."),
    MEMBER_WITHDRAWAL_NOT_ACTIVE(HttpStatus.CONFLICT, "MEMBER_WITHDRAWAL_NOT_ACTIVE", "ACTIVE 상태 회원만 탈퇴할 수 있습니다."),
    MEMBER_WITHDRAWAL_ACTIVE_ORDER_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_ACTIVE_ORDER_EXISTS",
            "진행 중인 주문이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_ACTIVE_PRODUCT_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_ACTIVE_PRODUCT_EXISTS",
            "판매 중인 상품이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_ACTIVE_AUCTION_EXISTS",
            "진행 중인 경매가 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_PENDING_AUCTION_PAYMENT_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_PENDING_AUCTION_PAYMENT_EXISTS",
            "결제 대기 중인 경매가 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_DELIVERY_IN_PROGRESS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_DELIVERY_IN_PROGRESS",
            "진행 중인 배송 건이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_PENDING_INCOME_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_PENDING_INCOME_EXISTS",
            "정산 대기 금액이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_PENDING_WITHDRAW_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_PENDING_WITHDRAW_EXISTS",
            "처리 중인 출금 요청이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_PENDING_SETTLEMENT_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_PENDING_SETTLEMENT_EXISTS",
            "정산 대기 건이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_PROCESSING_SETTLEMENT_EXISTS(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_PROCESSING_SETTLEMENT_EXISTS",
            "처리 중인 정산 건이 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_PARTIAL_SETTLEMENT_AVAILABLE(
            HttpStatus.CONFLICT,
            "MEMBER_WITHDRAWAL_PARTIAL_SETTLEMENT_AVAILABLE",
            "부분 정산 가능한 금액이 남아 있어 탈퇴할 수 없습니다."
    ),
    MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "MEMBER_WITHDRAWAL_CHECK_UNAVAILABLE",
            "회원 탈퇴 가능 여부를 확인할 수 없습니다. 잠시 후 다시 시도해 주세요."
    );

    private final HttpStatus status;
    private final String code;
    private final String message;

    MemberErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
