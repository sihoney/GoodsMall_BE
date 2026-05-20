package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeConfirmFailureCommand;
import com.example.payment.application.dto.ChargeConfirmFailureResult;

public interface ChargeConfirmFailureUseCase {

    ChargeConfirmFailureResult confirmChargeFailure(ChargeConfirmFailureCommand command);
}
