package com.example.payment.refund.application.usecase;

import com.example.payment.refund.application.dto.PaymentRefundResult;
import com.example.payment.refund.application.dto.SellerRefundCommand;

public interface SellerRefundUseCase {

    PaymentRefundResult requestSellerRefund(SellerRefundCommand command);
}
