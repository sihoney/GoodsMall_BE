package com.example.payment.wallet.application.usecase;

import com.example.payment.wallet.application.dto.CreateWalletCommand;
import com.example.payment.wallet.application.dto.CreateWalletResult;

/**
 * ?뚯썝 wallet ?앹꽦 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface CreateWalletUseCase {

    CreateWalletResult createWallet(CreateWalletCommand command);
}
