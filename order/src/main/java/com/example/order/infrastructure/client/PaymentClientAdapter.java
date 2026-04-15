package com.example.order.infrastructure.client;

import com.example.order.application.port.PaymentPort;
import com.example.order.application.port.dto.request.PaymentRefundRequest;
import com.example.order.application.port.dto.response.PaymentRefundResult;
import com.example.order.application.port.dto.request.PaymentRequest;
import com.example.order.application.port.dto.response.PaymentResult;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.infrastructure.client.dto.request.ExternalOrderLineRequest;
import com.example.order.infrastructure.client.dto.request.ExternalPaymentRefundLineRequest;
import com.example.order.infrastructure.client.dto.request.ExternalPaymentRefundRequest;
import com.example.order.infrastructure.client.dto.request.ExternalPaymentRequest;
import com.example.order.infrastructure.client.dto.response.PaymentRefundResultResponse;
import com.example.order.infrastructure.client.dto.response.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

    @Override
    public PaymentRefundResult requestRefund(PaymentRefundRequest request) {
        try {
            ExternalPaymentRefundRequest externalRequest = toExternalRefundRequest(request);
            PaymentRefundResultResponse response = paymentClient.requestRefund(externalRequest);
            return toPaymentRefundResult(response);
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
                request.paymentRequestOrderLines().stream()
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

    private PaymentRefundResult toPaymentRefundResult(PaymentRefundResultResponse response) {
        return new PaymentRefundResult(
                response.orderId(),
                response.refundedAmount(),
                PaymentStatus.from(response.status()),
                response.canceledAt(),
                response.failReason()
        );
    }

    private ExternalPaymentRefundRequest toExternalRefundRequest(PaymentRefundRequest request) {
        return new ExternalPaymentRefundRequest(
                request.orderId(),
                request.buyerMemberId(),
                UUID.randomUUID(),
                request.refundType(),
                request.reason(),
                request.items().stream()
                        .map(ExternalPaymentRefundLineRequest::from)
                        .toList()
        );
    }
}
