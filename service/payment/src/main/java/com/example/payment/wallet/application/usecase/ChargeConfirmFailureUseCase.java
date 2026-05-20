package com.example.payment.wallet.application.usecase;

import com.example.payment.wallet.application.dto.ChargeConfirmFailureCommand;
import com.example.payment.wallet.application.dto.ChargeConfirmFailureResult;

public interface ChargeConfirmFailureUseCase {

    ChargeConfirmFailureResult confirmChargeFailure(ChargeConfirmFailureCommand command);
}
