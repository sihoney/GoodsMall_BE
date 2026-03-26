package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.ChargeRefundResult;

/**
 * 충전 환불 유스케이스의 진입점이다.
 */
public interface ChargeRefundUseCase {

    ChargeRefundResult refundCharge(ChargeRefundCommand command);
}
