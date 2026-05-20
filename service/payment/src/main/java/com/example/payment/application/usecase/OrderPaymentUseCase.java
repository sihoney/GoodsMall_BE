package com.example.payment.application.usecase;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;

/**
 * 주문 결제 유스케이스의 진입점이다.
 */
public interface OrderPaymentUseCase {

    OrderPaymentResult payOrder(OrderPaymentCommand command);
}
