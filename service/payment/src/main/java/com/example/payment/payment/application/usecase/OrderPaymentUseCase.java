package com.example.payment.payment.application.usecase;

import com.example.payment.payment.application.dto.OrderPaymentCommand;
import com.example.payment.payment.application.dto.OrderPaymentResult;

/**
 * 二쇰Ц 寃곗젣 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface OrderPaymentUseCase {

    OrderPaymentResult payOrder(OrderPaymentCommand command);
}
