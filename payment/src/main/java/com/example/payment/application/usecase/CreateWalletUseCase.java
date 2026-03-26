package com.example.payment.application.usecase;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.dto.CreateWalletResult;

/**
 * 회원 wallet 생성 유스케이스의 진입점이다.
 */
public interface CreateWalletUseCase {

    CreateWalletResult createWallet(CreateWalletCommand command);
}
