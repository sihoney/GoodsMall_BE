package com.example.payment.wallet.application.usecase;

import com.example.payment.wallet.application.dto.ChargeConfirmCommand;
import com.example.payment.wallet.application.dto.ChargeConfirmResult;

/**
 * 異⑹쟾 ?뱀씤 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface ChargeConfirmUseCase {

    ChargeConfirmResult confirmCharge(ChargeConfirmCommand command);
}
