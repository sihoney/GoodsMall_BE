package com.example.payment.application.usecase;

import com.example.payment.presentation.dto.request.OrderPaymentApiRequest;
import com.example.payment.presentation.dto.response.OrderPaymentApiResponse;

/**
 * 주문 결제 API 진입 유스케이스다.
 * HTTP 요청 기반 주문 결제 처리의 application entrypoint 역할을 한다.
 */
public interface OrderPaymentApiUseCase {

    OrderPaymentApiResponse payOrder(OrderPaymentApiRequest request);
}
