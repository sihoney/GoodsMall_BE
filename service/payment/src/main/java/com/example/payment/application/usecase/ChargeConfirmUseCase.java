package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeConfirmResult;

/**
 * 충전 승인 유스케이스의 진입점이다.
 */
public interface ChargeConfirmUseCase {

    ChargeConfirmResult confirmCharge(ChargeConfirmCommand command);
}
