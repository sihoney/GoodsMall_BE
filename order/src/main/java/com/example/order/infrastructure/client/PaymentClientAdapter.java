package com.example.order.infrastructure.client;

import com.example.order.application.port.PaymentPort;
import com.example.order.application.port.PaymentRequest;
import com.example.order.application.port.PaymentResult;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.infrastructure.client.dto.request.ExternalOrderLineRequest;
import com.example.order.infrastructure.client.dto.request.ExternalPaymentRequest;
import com.example.order.infrastructure.client.dto.response.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements PaymentPort {

    private final PaymentClient paymentClient;

    @Override
    public PaymentResult requestPayment(PaymentRequest request) {
        try {
            ExternalPaymentRequest externalRequest = toExternalRequest(request);
            PaymentResultResponse response = paymentClient.requestPayment(externalRequest);

            return toPaymentResult(response);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    private ExternalPaymentRequest toExternalRequest(PaymentRequest request) {
        return new ExternalPaymentRequest(
                request.orderId(),
                request.buyerId(),
                request.totalPrice(),
                request.requestedAt(),
                request.orderLines().stream()
                        .map(line -> new ExternalOrderLineRequest(
                                line.orderItemId(),
                                line.sellerId(),
                                line.unitPriceSnapshot(),
                                line.quantity(),
                                line.lineTotalPrice()
                        ))
                        .toList()
        );
    }

    private PaymentResult toPaymentResult(PaymentResultResponse response) {
        return new PaymentResult(
                response.orderId(),
                response.amount(),
                PaymentStatus.from(response.status()),
                response.reasonCode()
        );
    }
}
