package com.example.payment.application.usecase;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.dto.CreateWalletResult;

public interface CreateWalletUseCase {

    CreateWalletResult createWallet(CreateWalletCommand command);
}
