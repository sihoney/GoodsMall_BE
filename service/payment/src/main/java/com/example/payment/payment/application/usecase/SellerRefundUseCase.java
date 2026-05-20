package com.example.payment.payment.application.usecase;

import com.example.payment.payment.application.dto.PaymentRefundResult;
import com.example.payment.payment.application.dto.SellerRefundCommand;

public interface SellerRefundUseCase {

    PaymentRefundResult requestSellerRefund(SellerRefundCommand command);
}
