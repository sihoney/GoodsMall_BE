package com.example.payment.wallet.application.usecase;

import com.example.payment.wallet.application.dto.ChargeCreateCommand;
import com.example.payment.wallet.application.dto.ChargeCreateResult;

/**
 * 異⑹쟾 ?붿껌 ?앹꽦 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface ChargeCreateUseCase {

    ChargeCreateResult createCharge(ChargeCreateCommand command);
}
