package com.example.order.application.processor;

import com.example.order.application.port.PaymentPort;
import com.example.order.application.port.dto.request.PaymentRequest;
import com.example.order.application.port.dto.request.PaymentRequestOrderLine;
import com.example.order.application.port.dto.response.PaymentResult;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentPort paymentPort;

    public void process(Order order) {
        PaymentRequest request = toPaymentRequest(order);
        PaymentResult result = paymentPort.requestPayment(request);
        validate(order, result);
    }

    private PaymentRequest toPaymentRequest(Order order) {
        return new PaymentRequest(
                order.getOrderId(),
                order.getBuyerId(),
                order.getTotalPrice(),
                Instant.now(),
                order.getItems().stream()
                        .map(item -> new PaymentRequestOrderLine(
                                item.getOrderItemId(),
                                item.getSellerId(),
                                item.getUnitPriceSnapshot(),
                                item.getQuantity(),
                                item.getTotalPrice(item.getUnitPriceSnapshot(), item.getQuantity())
                        ))
                        .toList()
        );
    }

    private void validate(Order order, PaymentResult result) {
        if (!result.orderId().equals(order.getOrderId())) {
            log.error("결제 응답 orderId 불일치. 요청={}, 응답={}", order.getOrderId(), result.orderId());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }

        if (result.status() != PaymentStatus.SUCCESS) {
            log.warn("결제 실패 orderId={}, 실패 이유={}", order.getOrderId(), result.reasonCode());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }

        if (result.paidAmount().compareTo(order.getTotalPrice()) != 0) {
            log.warn("결제 금액 불일치, 결제 해야할 금액={}, 결제된 금액={}", order.getTotalPrice(), result.paidAmount());
            throw new CustomException(ErrorCode.PAYMENT_FAILED);
        }
    }
}
