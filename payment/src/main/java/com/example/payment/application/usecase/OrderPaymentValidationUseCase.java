package com.example.payment.application.usecase;

import com.example.payment.application.dto.OrderPaymentValidationCommand;

public interface OrderPaymentValidationUseCase {

    boolean validateOrderPayment(OrderPaymentValidationCommand command);
}
