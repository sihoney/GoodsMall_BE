package com.example.payment.application.usecase;

import com.example.payment.application.dto.WithdrawCommand;
import com.example.payment.application.dto.WithdrawResult;

public interface WithdrawUseCase {

    WithdrawResult withdraw(WithdrawCommand command);
}
