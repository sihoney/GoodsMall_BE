package com.example.payment.infrastructure.client;

import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.domain.service.OrderPaymentValidationGateway;
import com.example.payment.infrastructure.client.dto.request.OrderPaymentValidationRequest;
import com.example.payment.infrastructure.client.dto.response.OrderPaymentValidationResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentValidationGatewayImpl implements OrderPaymentValidationGateway {

    private final OrderClient orderClient;

    public OrderPaymentValidationGatewayImpl(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public boolean validate(UUID orderId, Long amount) {
        try {
            OrderPaymentValidationResponse response = orderClient.validatePayment(
                    new OrderPaymentValidationRequest(orderId, amount)
            );
            // TODO: order 서비스와 협의 후 상태 코드, 오류 코드, 응답 포맷을 더 엄밀히 구분한다.
            return response != null && response.valid();
        } catch (RuntimeException e) {
            // TODO: 계약이 확정되면 order 미응답/4xx/5xx를 분리된 예외로 세분화한다.
            throw new InvalidCardPaymentRequestException("failed to validate order payment request.");
        }
    }
}
