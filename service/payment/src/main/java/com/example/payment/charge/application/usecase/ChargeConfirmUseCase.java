package com.example.payment.charge.application.usecase;

import com.example.payment.charge.application.dto.ChargeConfirmCommand;
import com.example.payment.charge.application.dto.ChargeConfirmResult;

/**
 * 異⑹쟾 ?뱀씤 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface ChargeConfirmUseCase {

    ChargeConfirmResult confirmCharge(ChargeConfirmCommand command);
}
