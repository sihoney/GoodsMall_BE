package com.example.payment.charge.application.usecase;

import com.example.payment.charge.application.dto.ChargeConfirmFailureCommand;
import com.example.payment.charge.application.dto.ChargeConfirmFailureResult;

public interface ChargeConfirmFailureUseCase {

    ChargeConfirmFailureResult confirmChargeFailure(ChargeConfirmFailureCommand command);
}
