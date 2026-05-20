package com.example.payment.payment.application.usecase;

import com.example.payment.payment.application.dto.OrderPaymentValidationCommand;
import com.example.payment.payment.domain.service.OrderPaymentValidationData;

public interface OrderPaymentValidationUseCase {

    OrderPaymentValidationData validateOrderPayment(OrderPaymentValidationCommand command);
}
