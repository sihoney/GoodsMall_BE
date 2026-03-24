package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeConfirmResult;

public interface ChargeConfirmUseCase {

    ChargeConfirmResult confirmCharge(ChargeConfirmCommand command);
}
