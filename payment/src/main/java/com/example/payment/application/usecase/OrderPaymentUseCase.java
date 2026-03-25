package com.example.payment.application.usecase;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;

public interface OrderPaymentUseCase {

    OrderPaymentResult payOrder(OrderPaymentCommand command);
}
