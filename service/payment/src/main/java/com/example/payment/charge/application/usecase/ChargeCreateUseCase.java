package com.example.payment.charge.application.usecase;

import com.example.payment.charge.application.dto.ChargeCreateCommand;
import com.example.payment.charge.application.dto.ChargeCreateResult;

/**
 * 異⑹쟾 ?붿껌 ?앹꽦 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface ChargeCreateUseCase {

    ChargeCreateResult createCharge(ChargeCreateCommand command);
}
