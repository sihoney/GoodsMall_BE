package com.example.order.domain.enumtype;

import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;

public enum PaymentStatus {
    SUCCESS,
    FAILED;

    public static PaymentStatus from(String value) {
        try {
            return PaymentStatus.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }
    }
}
