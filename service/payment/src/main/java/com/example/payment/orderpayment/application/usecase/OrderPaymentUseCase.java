package com.example.payment.orderpayment.application.usecase;

import com.example.payment.orderpayment.application.dto.OrderPaymentCommand;
import com.example.payment.orderpayment.application.dto.OrderPaymentResult;

/**
 * 二쇰Ц 寃곗젣 ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface OrderPaymentUseCase {

    OrderPaymentResult payOrder(OrderPaymentCommand command);
}
