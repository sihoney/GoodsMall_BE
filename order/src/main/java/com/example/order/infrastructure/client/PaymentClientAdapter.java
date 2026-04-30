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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements PaymentPort {

    private final PaymentClient paymentClient;

    @Override
    public PaymentResult requestPayment(PaymentRequest request) {
        try {
            ExternalPaymentRequest externalRequest = toExternalRequest(request);
            PaymentResultResponse response = paymentClient.requestPayment(externalRequest).data();

            return toPaymentResult(response);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }

    @Override
    public PaymentRefundResult requestRefund(PaymentRefundRequest request) {
        try {
            ExternalPaymentRefundRequest externalRequest = toExternalRefundRequest(request);
            PaymentRefundResultResponse response = paymentClient.requestRefund(externalRequest).data();
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
                response.totalRefundAmount(),
                toPaymentStatus(response.refundStatus()),
                response.processedAt(),
                response.failReason()
        );
    }

    private PaymentStatus toPaymentStatus(String refundStatus) {
        if (refundStatus == null) {
            log.error("환불 응답 status가 null. 실패로 처리합니다.");
            return PaymentStatus.FAILED;
        }

        String upper = refundStatus.toUpperCase();

        if ("SUCCEEDED".equals(upper)) {
            return PaymentStatus.SUCCESS;
        }

        if ("FAILED".equals(upper)) {
            return PaymentStatus.FAILED;
        }

        if ("PROCESSING".equals(upper) || "REQUESTED".equals(upper)) {
            // PG 일시 장애 등으로 동기 응답이 처리 중인 케이스. 보수적으로 실패 처리하여 재시도 유도.
            log.warn("환불이 처리 중 상태로 응답됨. 실패로 처리하여 재시도 유도. status={}", refundStatus);
            return PaymentStatus.FAILED;
        }

        log.error("알 수 없는 환불 status. status={}", refundStatus);
        return PaymentStatus.FAILED;
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
