package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeCreateResult;

public interface ChargeCreateUseCase {

    ChargeCreateResult createCharge(ChargeCreateCommand command);
}
