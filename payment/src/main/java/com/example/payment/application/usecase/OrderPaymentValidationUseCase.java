package com.example.payment.application.usecase;

import com.example.payment.application.dto.OrderPaymentValidationCommand;
import com.example.payment.domain.service.OrderPaymentValidationData;

public interface OrderPaymentValidationUseCase {

    OrderPaymentValidationData validateOrderPayment(OrderPaymentValidationCommand command);
}
