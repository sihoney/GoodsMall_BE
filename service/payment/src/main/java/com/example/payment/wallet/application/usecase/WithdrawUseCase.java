package com.example.payment.wallet.application.usecase;

import com.example.payment.wallet.application.dto.WithdrawCommand;
import com.example.payment.wallet.application.dto.WithdrawResult;

public interface WithdrawUseCase {

    WithdrawResult withdraw(WithdrawCommand command);
}
