package com.example.payment.application.usecase;

import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.ChargeRefundResult;

public interface ChargeRefundUseCase {

    ChargeRefundResult refundCharge(ChargeRefundCommand command);
}
