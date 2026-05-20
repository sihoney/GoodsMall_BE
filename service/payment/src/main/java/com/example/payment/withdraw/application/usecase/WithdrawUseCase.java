package com.example.payment.withdraw.application.usecase;

import com.example.payment.withdraw.application.dto.WithdrawCommand;
import com.example.payment.withdraw.application.dto.WithdrawResult;

public interface WithdrawUseCase {

    WithdrawResult withdraw(WithdrawCommand command);
}
