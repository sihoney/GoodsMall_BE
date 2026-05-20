package com.example.payment.orderpayment.application.usecase;

import com.example.payment.orderpayment.application.dto.OrderPaymentValidationCommand;
import com.example.payment.orderpayment.domain.service.OrderPaymentValidationData;

public interface OrderPaymentValidationUseCase {

    OrderPaymentValidationData validateOrderPayment(OrderPaymentValidationCommand command);
}
