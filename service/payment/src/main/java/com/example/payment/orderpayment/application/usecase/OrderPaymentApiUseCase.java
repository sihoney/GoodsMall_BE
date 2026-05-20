package com.example.payment.orderpayment.application.usecase;

import com.example.payment.orderpayment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.orderpayment.presentation.dto.response.OrderPaymentApiResponse;

/**
 * 二쇰Ц 寃곗젣 API 吏꾩엯 ?좎뒪耳?댁뒪??
 * HTTP ?붿껌 湲곕컲 二쇰Ц 寃곗젣 泥섎━??application entrypoint ??븷???쒕떎.
 */
public interface OrderPaymentApiUseCase {

    OrderPaymentApiResponse payOrder(OrderPaymentApiRequest request);
}
