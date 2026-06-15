package com.example.payment.payment.application.usecase;

import com.example.payment.payment.application.dto.PaymentRefundCommand;
import com.example.payment.payment.application.dto.PaymentRefundResult;

public interface PaymentCancellationUseCase {

    PaymentRefundResult requestCancellation(PaymentRefundCommand command);
}
