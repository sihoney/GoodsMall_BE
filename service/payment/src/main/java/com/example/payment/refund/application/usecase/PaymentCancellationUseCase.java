package com.example.payment.refund.application.usecase;

import com.example.payment.refund.application.dto.PaymentRefundCommand;
import com.example.payment.refund.application.dto.PaymentRefundResult;

public interface PaymentCancellationUseCase {

    PaymentRefundResult requestCancellation(PaymentRefundCommand command);
}
