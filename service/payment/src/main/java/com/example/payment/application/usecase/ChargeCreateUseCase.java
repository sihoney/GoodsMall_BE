package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeCreateResult;

/**
 * 충전 요청 생성 유스케이스의 진입점이다.
 */
public interface ChargeCreateUseCase {

    ChargeCreateResult createCharge(ChargeCreateCommand command);
}
