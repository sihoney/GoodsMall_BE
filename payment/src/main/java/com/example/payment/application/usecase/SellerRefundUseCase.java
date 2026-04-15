package com.example.payment.application.usecase;

import com.example.payment.application.dto.PaymentRefundResult;
import com.example.payment.application.dto.SellerRefundCommand;

public interface SellerRefundUseCase {

    PaymentRefundResult requestSellerRefund(SellerRefundCommand command);
}
